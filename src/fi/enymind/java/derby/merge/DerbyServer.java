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

import java.net.InetAddress;
import java.util.Properties;
import org.apache.derby.drda.NetworkServerControl;

public class DerbyServer {

    private String databasePassword;

    public DerbyServer(Properties properties) {
        this.databasePassword = properties.getProperty("databasePassword");
        thread = new DerbyServerThread(this.databasePassword);
        thread.start();
    }

    public class DerbyServerThread extends Thread {

        private String databasePassword;

        public DerbyServerThread(String databasePassword) {
            this.databasePassword = databasePassword;
        }

        @Override
        public void run() {
            try {
                NetworkServerControl server = new NetworkServerControl(InetAddress.getByName("0.0.0.0"), 1527, "APP", this.databasePassword);
                server.start(null);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }
    DerbyServerThread thread;
}
