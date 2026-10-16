# Online Crowdfunding Platform (Java Swing + JDBC)

A desktop crowdfunding application built with **Java Swing** for the user
interface and **JDBC + MySQL** for the database. It was built to cover the
"Java GUI Based Projects" marking rubric.

## How the project maps to the rubric

| Rubric criterion | Where it lives |
|---|---|
| **OOP** - inheritance, polymorphism, interfaces, exception handling | `User` is an abstract base class; `Admin`, `Creator`, `Contributor` extend it and override `getRole()` / `getDashboardTitle()`. `DAO<T>` is a generic interface. Custom checked exceptions `DataAccessException` and `ValidationException`, plus try-with-resources everywhere. |
| **Collections & Generics** | Generic `List<T>` / `ArrayList` throughout, `HashMap` for settings, generic `DAO<T>` interface. |
| **Multithreading & Synchronization** | `ActivityLogger` (producer/consumer log thread using `synchronized` + `wait/notify`), `CampaignMonitor` (background thread that refreshes totals and updates the UI with `SwingUtilities.invokeLater`). |
| **Classes for database operations** | `db` package: `UserDAO`, `CampaignDAO`, `ContributionDAO`, `SettingsDAO`, `ActivityLogDAO`. |
| **Database Connectivity (JDBC)** | `DBConnection` opens connections; every DAO uses `PreparedStatement` and `ResultSet`. |
| **Implement JDBC** | The whole `db` package plus `sql/schema.sql`. |

## Requirements

- **Java JDK 17 or newer**
- **MySQL Server** (running locally) and the MySQL Connector/J driver
  (already included in `lib/`)

## Step 1 - Create the database

Make sure MySQL is running, then run the schema script:

```bash
mysql -u root -p < sql/schema.sql
```

(You can also open `sql/schema.sql` in MySQL Workbench and run it.)

This creates the `crowdfund` database, all the tables, the default admin
account and the default settings.

## Step 2 - Tell the app your MySQL password

The app defaults to user `root` with password `root`. If your MySQL password
is different, you have two options.

**Option A - pass it on the command line (recommended):**

```bash
java -cp "lib/mysql-connector-j-8.4.0.jar:out" -Ddb.user=root -Ddb.password=YOURPASSWORD com.crowdfund.Main
```

(On Windows use `;` instead of `:` between the two classpath entries.)

**Option B - edit the defaults** in `src/com/crowdfund/db/DBConnection.java`.

## Step 3 - Compile and run

From the project folder:

```bash
# compile
javac -cp lib/mysql-connector-j-8.4.0.jar -d out $(find src -name "*.java")

# run
java -cp "lib/mysql-connector-j-8.4.0.jar:out" com.crowdfund.Main
```

On Windows (PowerShell), compile with:

```powershell
javac -cp "lib/mysql-connector-j-8.4.0.jar" -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp "lib/mysql-connector-j-8.4.0.jar;out" com.crowdfund.Main
```

### Default login

```
Email:    admin@crowd.com
Password: admin123
```

Creators and contributors register themselves from the login window.

## What each role can do

**Admin** - manage users (add / delete), approve or reject campaigns, edit
system settings, and watch the activity log.

**Campaign Creator** - create campaigns, see their progress, post update
messages, and view analytics.

**Contributor** - browse approved campaigns, contribute, track their
contribution history, read updates, and edit their profile.

Every window has a status bar at the bottom showing live platform totals,
refreshed by a background thread.

## Folder structure

```
CrowdfundingPlatform/
├── src/com/crowdfund/
│   ├── Main.java                 (program entry point)
│   ├── model/                    (User + subclasses, Campaign, Contribution)
│   ├── db/                       (DBConnection, DAO interface, DAO classes)
│   ├── service/                  (UserFactory, ActivityLogger, CampaignMonitor)
│   ├── exception/                (DataAccessException, ValidationException)
│   └── ui/                       (LoginFrame + three dashboards)
├── sql/schema.sql                (the database schema)
├── lib/mysql-connector-j-8.4.0.jar
├── old-versions/                 (earlier console app and web version)
└── README.md
```

## Note about the database

The `lib/` folder contains the MySQL Connector/J jar so the project runs
out of the box. When you put this on GitHub you would normally add the driver
through a build tool (Maven/Gradle) instead of committing the jar - but for a
college project, keeping it in `lib/` is the simplest way to make it runnable.
