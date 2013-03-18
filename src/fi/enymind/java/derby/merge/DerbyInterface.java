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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.util.Properties;
import java.util.TreeMap;

public class DerbyInterface {

    private DerbyConnection mdbc;
    private Statement stmt;
    private boolean connected = false;
    private Properties properties;

    public DerbyInterface(Properties properties, TreeMap<String, TreeMap<String, String>> sructure) throws SQLException, ClassNotFoundException {
        this.mdbc = new DerbyConnection(properties);
        this.properties = properties;
        try {
            this.mdbc.init();
        } catch (SQLException e) {
            if (e.getMessage().indexOf("not found") > -1) {
                this.invokeCreator(sructure);
                this.reload(false);
            } else {
                throw e;
            }
        }
        Connection conn = this.mdbc.getConnection();
        this.stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

        this.connected = true;
    }

    public void reload(boolean onlyWake) throws SQLException, ClassNotFoundException {
        if (!onlyWake) {
            this.Close();
        }

        this.mdbc = new DerbyConnection(this.properties);
        this.mdbc.init();
        Connection conn = this.mdbc.getConnection();
        this.stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

        this.connected = true;
    }

    public void Close() throws SQLException {
        this.mdbc.close(this.stmt);
        this.mdbc.destroy();

        this.connected = false;
    }

    public synchronized ResultSet Query(String query) throws SQLException, ClassNotFoundException {
        ResultSet resultSet = null;

        try {
            if (this.connected) {
                resultSet = this.stmt.executeQuery(query);
            }
        } catch (SQLNonTransientConnectionException e) {
            this.connected = false;
            this.reload(false);
        }

        return resultSet;
    }

    public synchronized int SilentQuery(String query) throws SQLException, ClassNotFoundException {
        int result = -1;

        try {
            if (this.connected) {
                result = this.stmt.executeUpdate(query);
            }
        } catch (SQLNonTransientConnectionException e) {
            this.connected = false;
            this.reload(false);
        }

        return result;
    }

    public boolean invokeCreator(TreeMap<String, TreeMap<String, String>> sructure) throws SQLException, ClassNotFoundException {
        return this.mdbc.invokeCreator(sructure);
    }
}