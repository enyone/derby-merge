/*
 * (C) Copyright 2013 Enymind Oy (http://www.enymind.fi/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 * Juho Tykkälä juho.tykkala@enymind.fi
 */
package fi.enymind.java.derby.merge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

public class DerbyDatabaseCreator {

    private Connection connection;
    private TreeMap<String, TreeMap<String, String>> databaseSructure;
    private String databaseLocation;
    private String databasePassword;

    public DerbyDatabaseCreator(Connection connection) {
        this.connection = connection;
    }

    public boolean create(Properties properties) throws SQLException, ClassNotFoundException {
        this.databaseLocation = properties.getProperty("databaseLocation");
        this.databasePassword = properties.getProperty("databasePassword");

        if (this.connection == null) {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

            Properties props = new Properties();
            props.put("user", "APP");
            props.put("password", this.databasePassword);

            this.connection = DriverManager.getConnection("jdbc:derby:" + this.databaseLocation + "/database/default;create=true", props);
        }

        java.sql.Statement stmt = this.connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

        ArrayList<String> currentTables = new ArrayList<String>();
        ResultSet allTables = stmt.executeQuery("select tablename from sys.systables where tabletype = 'T'");
        while (allTables.next()) {
            currentTables.add(allTables.getString("tablename").toLowerCase());
        }

        TreeMap<String, TreeMap<String, String>> sructure = getDatabaseStructure();
        Iterator it = sructure.keySet().iterator();
        while (it.hasNext()) {
            String tableName = (String) it.next();
            TreeMap<String, String> columns = sructure.get(tableName);

            if (tableName.equals("z-indexes")) {
                ArrayList<String> currentIndexes = new ArrayList<String>();
                ResultSet allIndexes = stmt.executeQuery("select conglomeratename from sys.sysconglomerates c left join sys.systables t on t.tableid = c.tableid where c.isindex = true");
                while (allIndexes.next()) {
                    currentIndexes.add(allIndexes.getString("conglomeratename").toLowerCase());
                }

                Iterator it2 = columns.keySet().iterator();
                while (it2.hasNext()) {
                    String indexName = (String) it2.next();
                    String indexType = sructure.get(tableName).get(indexName);

                    if (!currentIndexes.contains(indexName.toLowerCase())) {
                        stmt.executeUpdate("CREATE INDEX " + indexName + " ON app." + indexType);
                    }
                }
            } else {
                if (!currentTables.contains(tableName.toLowerCase())) {
                    stmt.executeUpdate("create table app." + tableName + " ( id int not null generated always as identity ( start with 1, increment by 1 ), primary key ( id ) )");
                }

                ArrayList<String> currentColumns = new ArrayList<String>();
                ResultSet allColumns = stmt.executeQuery("select columnname from sys.syscolumns c left join sys.systables t on t.tableid = c.referenceid where t.tablename = '" + tableName.toUpperCase() + "'");
                while (allColumns.next()) {
                    currentColumns.add(allColumns.getString("columnname").toLowerCase());
                }

                Iterator it2 = columns.keySet().iterator();
                while (it2.hasNext()) {
                    String columnName = (String) it2.next();
                    String columnType = sructure.get(tableName).get(columnName);

                    if (!currentColumns.contains(columnName.toLowerCase())) {
                        stmt.executeUpdate("alter table app." + tableName + " add column " + columnName + " " + columnType);
                    }
                }
            }
        }

        return true;
    }

    private TreeMap<String, TreeMap<String, String>> getDatabaseStructure() {
        return this.databaseSructure;
    }

    public void setDatabaseStructure(TreeMap<String, TreeMap<String, String>> databaseSructure) {
        this.databaseSructure = databaseSructure;
    }
}