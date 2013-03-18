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
import fi.enymind.java.derby.merge.DerbyInterface;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TreeMap;

public class DerbyMergeClient {

    public static void main(String[] args) {
        // Initialize needed properties
        Properties props = new Properties();
        props.put("isRemoteConnection", "false");
        props.put("databaseLocation", System.getProperty("user.dir"));
        props.put("databasePassword", "abc123DEF");

        try {
            // Construct database structure
            TreeMap<String, TreeMap<String, String>> sructure = new TreeMap<String, TreeMap<String, String>>();

            // Populate some tables (mytable1)
            TreeMap<String, String> mytable1Columns = new TreeMap<String, String>();
            mytable1Columns.put("name", "varchar(16) not null default 'Noname'");
            mytable1Columns.put("value", "varchar(64) not null default 'Novalue'");
            sructure.put("mytable1", mytable1Columns);

            // Create database interface with properties and structure
            DerbyInterface dbi = new DerbyInterface(props, sructure);

            // Invoce creator with structure
            dbi.invokeCreator(sructure);

            // Make simple insert query
            dbi.SilentQuery("insert into mytable1 (name, value) values ('Barbra', 'Streisand')");

            // Get data from database
            ResultSet data = dbi.Query("select name, value from mytable1");

            // Loop through result
            while (data.next()) {
                StringBuilder result = new StringBuilder();
                result.append("name = ");
                result.append(data.getString("name"));
                result.append(", value = ");
                result.append(data.getString("value"));

                System.out.println(result.toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
