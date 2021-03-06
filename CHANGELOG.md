Change Log
==========

  Version 3.2.0 *(2015-04)*
----------------------------

 * Support for sqlQueryBuilderSupport flag on dbtools-gen (used when generating template view and query objects)
 * Allow entitites passed to AndroidBaseManager to be null
 
  Version 3.1.0 *(2015-02)*
----------------------------

 * Be sure to use dbtools-gen-3.1.1.jar
 * Improved loading of sqliteX library
 * Improvements to findCursorBySelection(...)
 * Bug fixes and performance improvements

  Version 3.0.0 *(2015-01)*
----------------------------

 * Changed DBTools to use DatabaseWrapper interface to identify the type of SQLite Database to use (Build-in Android, SQLCipher, SQLite.org build of SQLite, etc).  Allows the use of any custom build of SQLite.
 * Added several default implementations of DatabaseWrapper interface (AndroidDatabaseWrapper, SQLCipherDatabaseWrapper, SQLiteDatabaseWrapper)
 * Removed unneeded
 * Minor bug fixes from 2.6.0


  Version 2.6.0 *(2015-01)*
----------------------------

 * Consolidated all of the different findXXXBySelection(...) to findValueBySelection(...) to allow better support for more datatypes
 * Migration to 2.6.0

   findXXXBySelection(...) to findValueBySelection(class, ...)
   findXXXByRawQuery(...) to findValueByRawQuery(class, ...)
   findAllIntByRawQuery(...) to findAllValuesByRawQuery(class, ...)
   NEW: findAllValuesBySelection(class, ...)

  Version 2.5.4 *(2015-01)*
----------------------------

 * Added findLongBySelection(...), findIntBySelection(...), findStringBySelection(...), findBooleanBySelection(...), findDateBySelection(...), findDateTimeBySelection(...) 
 * Improved findXXXByRawQuery(...) support for Int, Boolean, Date, DateTime
 * Fix for issue #5 (ConnectAllDatabases should call IdentifyDatabases)


  Version 2.5.2 *(2014-11)*
----------------------------

 * Removed deprecated methods
 * Minor improvements to AndroidBaseManager insert(...) update(...) delete(...) methods
 * Bug fixes

  Version 2.5.0 *(2014-10)*
----------------------------

 * NEW Support for Async writes (insertAsync, updateAsync, deleteAsync, saveAsync)

  Version 2.3.0 *(2014-10)*
----------------------------

  * NEW Query database type (ability to query across attached databases)
  * NEW Merge cursor support
  * NEW Matrix cursor support
  * NEW JSR 305 support (@Nullable / @Notnull)
  * NEW Otto Event But support
  * Improved support for pulling data from a Cursor (Individual.getName(cursor))
  * Bug fixes



Version 2.1.0 *(2014-05-15)*
----------------------------

 * NEW JSR 305 support (@Nullable / @Nonnull)
 * GENERATOR: Fixed issues with databases that have a '.' in the name
 * GENERATOR: Changed getColumnIndex(...) to getColumnIndexOrThrow(...) for better error messages
 
 Version 2.0.0 *(2014-04)*
----------------------------

 * NEW DatabaseBaseManager.java is generated from dbtools-gen Gradle/Android plugin
 * NEW DatabaseBaseManager.java creates
 * NEW View Support <view/> element added to schema.xml file
 * dbschema.xsd is auto-updated from dbtools-gen Gradle/Android plugin
 * Bug fixes

