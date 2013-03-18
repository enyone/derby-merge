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
import java.util.Properties;
import java.util.TreeMap;

public class DerbyConnection {

    private Connection connection;
    private boolean isRemoteConnection;
    private String databaseHost;
    private String databaseLocation;
    private String databasePassword;
    private Properties properties;

    public DerbyConnection(Properties properties) {
        this.properties = properties;
        this.isRemoteConnection = (properties.getProperty("isRemoteConnection").equals("true")) ? true : false;
        if (this.isRemoteConnection) {
            this.databaseHost = properties.getProperty("databaseHost");
        } else {
            this.databaseLocation = properties.getProperty("databaseLocation");
        }
        this.databasePassword = properties.getProperty("databasePassword");
    }

    public void init() throws SQLException, ClassNotFoundException {
        if (this.isRemoteConnection) {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
        } else {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        }

        if (this.isRemoteConnection) {
            Properties props = new Properties();
            props.put("user", "APP");
            props.put("password", this.databasePassword);

            this.connection = DriverManager.getConnection("jdbc:derby://" + this.databaseHost + ":1527/database/default", props);
        } else {
            Properties props = new Properties();
            props.put("user", "APP");
            props.put("password", this.databasePassword);

            this.connection = DriverManager.getConnection("jdbc:derby:" + this.databaseLocation + "/database/default", props);
        }
    }

    public boolean invokeCreator(TreeMap<String, TreeMap<String, String>> sructure) throws SQLException, ClassNotFoundException {
        DerbyDatabaseCreator creator = new DerbyDatabaseCreator(connection);
        creator.setDatabaseStructure(sructure);
        return creator.create(this.properties);
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void close(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }

    public void close(java.sql.Statement stmt) throws SQLException {
        if (stmt != null) {
            stmt.close();
        }
    }

    public void destroy() throws SQLException {
        if (this.connection != null) {
            this.connection.close();
            DriverManager.getConnection("jdbc:derby:" + this.databaseLocation + "/database/default;shutdown=true");
        }
    }
}