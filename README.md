derby-merge
===========

Java library used with Apache Derby database engine when dynamically
creating or migrating database schemas into existing database.

Supports creating a new database from scratch.

Supports creating new tables and table columns. Does not support altering
table columns nor deleting tables or columns.

#### License ####

All rights reserved. This program and the accompanying materials
are made available under the terms of the GNU Lesser General Public License
(LGPL) version 2.1 which accompanies this distribution, and is available at
http://www.gnu.org/licenses/lgpl-2.1.html

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

#### Usage ####

Initialize needed properties

```java
Properties props = new Properties();
props.put("isRemoteConnection", "false");
props.put("databaseLocation", System.getProperty("user.dir"));
props.put("databasePassword", "abc123DEF");
```

Construct database structure

```java
TreeMap<String, TreeMap<String, String>> sructure = new TreeMap<String, TreeMap<String, String>>();
```

Populate some tables (mytable1)

```java
TreeMap<String, String> mytable1Columns = new TreeMap<String, String>();
mytable1Columns.put("name", "varchar(16) not null default 'Noname'");
mytable1Columns.put("value", "varchar(64) not null default 'Novalue'");
sructure.put("mytable1", mytable1Columns);
```

Create database interface with properties and structure

```java
DerbyInterface dbi = new DerbyInterface(props, sructure);
```

Invoce creator with structure

```java
dbi.invokeCreator(sructure);
```

See complete and working demo at file DerbyMergeClinet.java
