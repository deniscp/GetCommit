# GetCommit

GetCommit fetches and stores informations about every commit committed since a given date for every repository of a given list of repositories.
It creates a table named Commits on a given database in which it stores some values: Full_name, Sha, Date, Committed_by, File and Commit.
Full name field is in the form "organization name/repository name" (or "user name/repository name"), Commit field contains all informations about the commit.

The sintax must be the following: "Test https://url dd-MM-yyyy"; url represents the GitHub api https url of the list of organization (or list of user) repositories and dd-MM-yyyy represents the format of the date since commits have been committed.

By default jdbc database url is "jdbc:postgres://localhost:5432/GetIssues" and login and password are set to "postgres".
If you want to use a jdbc driver prior to version 4.0 you will need to uncomment "Class.forName(...)" and "catch(ClassNotFoundException ex){...}" blocks in the openDB() method of GetIssuess.java.
