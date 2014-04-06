package org.dbtools.android.domain;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

public abstract class AndroidDatabaseBaseManager {
    public static final String TAG = "AndroidDBTools";

    private Map<String, AndroidDatabase> databaseMap = new HashMap<String, AndroidDatabase>(); // <Database name, Database Path>

    /**
     * Identify what databases will be used with this app.  This method should call addDatabase(...) for each database
     */
    public abstract void identifyDatabases();
    public abstract void onCreate(AndroidDatabase androidDatabase);
    public abstract void onCreateViews(AndroidDatabase androidDatabase);
    public abstract void onDropViews(AndroidDatabase androidDatabase);
    public abstract void onUpgrade(AndroidDatabase androidDatabase, int oldVersion, int newVersion);

    /**
     * Add a standard SQLite database
     * @param context Android Context
     * @param databaseName Name of the database
     * @param version Version of the database
     */
    public void addDatabase(Context context, String databaseName, int version, int viewsVersion) {
        String databasePath = getDatabaseFile(context, databaseName).getAbsolutePath();
        databaseMap.put(databaseName, new AndroidDatabase(databaseName, databasePath, version, viewsVersion));
    }

    /**
     * Add a standard SQLite database
     * @param databaseName Name of the attached database
     * @param primaryDatabaseName Primary Database name
     * @param attachedDatabaseNames Database names to be attached to Primary Database
     */
    public void addAttachedDatabase(String databaseName, String primaryDatabaseName, List<String> attachedDatabaseNames) {
        AndroidDatabase primaryDatabase = getDatabase(primaryDatabaseName);
        if (primaryDatabase == null) {
            throw new IllegalStateException("Database [" + primaryDatabaseName + "] does not exist");
        }

        List<AndroidDatabase> attachedDatabases = new ArrayList<AndroidDatabase>();
        for (String databaseNameToAttach : attachedDatabaseNames) {
            AndroidDatabase databaseToAttach = getDatabase(databaseNameToAttach);
            if (databaseToAttach == null) {
                throw new IllegalStateException("Database to attach [" + databaseNameToAttach + "] does not exist");
            }

            attachedDatabases.add(databaseToAttach);
        }

        databaseMap.put(databaseName, new AndroidDatabase(databaseName, primaryDatabase, attachedDatabases));
    }

    /**
     * Add a standard SQLite database
     * @param databaseName Name of the attached database
     * @param primaryDatabase Primary Database
     * @param attachedDatabases Databases to be attached to Primary Database
     */
    public void addAttachedDatabase(String databaseName, AndroidDatabase primaryDatabase, List<AndroidDatabase> attachedDatabases) {
        databaseMap.put(databaseName, new AndroidDatabase(databaseName, primaryDatabase, attachedDatabases));
    }

    public void removeAttachedDatabase(String databaseName) {
        AndroidDatabase database = databaseMap.get(databaseName);
        if (database != null) {
            closeDatabase(database);
            databaseMap.remove(databaseName);
        }
    }

    /**
     * Add a SQLCipher SQLite database.  If the password is null, then a standard SQLite database will be used
     * @param context Android Context
     * @param databaseName Name of the database
     * @param password Database password
     * @param version Version of the database
     */
    public void addDatabase(Context context, String databaseName, String password, int version, int viewsVersion) {
        String databasePath = getDatabaseFile(context, databaseName).getAbsolutePath();

        if (password != null) {
            databaseMap.put(databaseName, new AndroidDatabase(databaseName, password, databasePath, version, viewsVersion));
        } else {
            databaseMap.put(databaseName, new AndroidDatabase(databaseName, databasePath, version, viewsVersion));
        }
    }

    public void addDatabase(AndroidDatabase database) {
        databaseMap.put(database.getName(), database);
    }

    /**
     * Reset/Removes any references to any database that was added
     */
    public void reset() {
        databaseMap = new HashMap<String, AndroidDatabase>();
    }

    public Collection<AndroidDatabase> getDatabases() {
        return databaseMap.values();
    }

    private void createDatabaseMap() {
        if (databaseMap == null || databaseMap.size() == 0) {
            identifyDatabases();
        }
    }

    public String getDatabasePath(String databaseName) {
        return databaseMap.get(databaseName).getPath();
    }

    public AndroidDatabase getDatabase(String databaseName) {
        return databaseMap.get(databaseName);
    }

    private boolean isDatabaseAlreadyOpen(AndroidDatabase db) {
        if (!db.isEncrypted()) {
            return AndroidBaseManager.isDatabaseAlreadyOpen(db);
        } else {
            return org.dbtools.android.domain.secure.AndroidBaseManager.isDatabaseAlreadyOpen(db);
        }
    }

    /**
     * Helper method to load the SQLCipher Libraries
     * @param context Android Context
     */
    public void initSQLCipherLibs(Context context) {
        net.sqlcipher.database.SQLiteDatabase.loadLibs(context); // Initialize SQLCipher
    }

    /**
     * Helper method to load the SQLCipher Libraries
     * @param context Android Context
     * @param workingDir Directory to extract SQLCipher files (such as an external storage space)
     */
    public void initSQLCipherLibs(Context context, File workingDir) {
        net.sqlcipher.database.SQLiteDatabase.loadLibs(context, workingDir); // Initialize SQLCipher
    }

    public void openDatabase(AndroidDatabase androidDatabase) {
        if (!androidDatabase.isEncrypted()) {
            AndroidBaseManager.openDatabase(androidDatabase);
        } else {
            org.dbtools.android.domain.secure.AndroidBaseManager.openDatabase(androidDatabase);
        }
    }

    public void closeDatabase(String databaseName) {
        closeDatabase(getDatabase(databaseName));
    }

    public boolean closeDatabase(AndroidDatabase androidDatabase) {
        if (!androidDatabase.isEncrypted()) {
            return AndroidBaseManager.closeDatabase(androidDatabase);
        } else {
            return org.dbtools.android.domain.secure.AndroidBaseManager.closeDatabase(androidDatabase);
        }
    }

    public void connectAllDatabases() {
        Collection<AndroidDatabase> databases = getDatabases();
        for (AndroidDatabase database : databases) {
            connectDatabase(database.getName());
        }
    }

    public void connectDatabase(String databaseName) {
        connectDatabase(databaseName, true);
    }

    public synchronized void connectDatabase(String databaseName, boolean checkForUpgrade) {
        createDatabaseMap();

        AndroidDatabase androidDatabase = databaseMap.get(databaseName);

        if (androidDatabase == null) {
            throw new IllegalArgumentException("Cannot connect to database (Cannot find database [" + databaseName + "] in databaseMap (databaseMap size: " + databaseMap.size() + ")).  Was this database added in the identifyDatabases() method?");
        }

        boolean databaseAlreadyExists = new File(androidDatabase.getPath()).exists();
        if (!databaseAlreadyExists || !isDatabaseAlreadyOpen(androidDatabase)) {
            String databasePath = androidDatabase.getPath();
            File databaseFile = new File(databasePath);
            boolean databaseExists = databaseFile.exists();
            Log.i(TAG, "Connecting to database");
            Log.i(TAG, "Database exists: " + databaseExists + "(path: " + databasePath + ")");

            // if this is an attached database, make sure the main database is open first
            AndroidDatabase attachMainDatabase = androidDatabase.getAttachMainDatabase();
            if (attachMainDatabase != null) {
                connectDatabase(attachMainDatabase.getName());
            }

            openDatabase(androidDatabase);

            if (!androidDatabase.isAttached()) {
                // if the database did not already exist, just created it, otherwise perform upgrade path
                if (!databaseAlreadyExists) {
                    createMetaTableIfNotExists(androidDatabase);
                    onCreate(androidDatabase);
                    onCreateViews(androidDatabase);
                } else if (checkForUpgrade) {
                    if (androidDatabase.isEncrypted()) {
                        onUpgrade(androidDatabase, androidDatabase.getSecureSqLiteDatabase().getVersion(), androidDatabase.getVersion());
                    } else {
                        onUpgrade(androidDatabase, androidDatabase.getSqLiteDatabase().getVersion(), androidDatabase.getVersion());
                    }

                    onUpgradeViews(androidDatabase, findViewVersion(androidDatabase), androidDatabase.getViewsVersion());
                }

                // update database and views versions
                if (androidDatabase.isEncrypted()) {
                    androidDatabase.getSecureSqLiteDatabase().setVersion(androidDatabase.getVersion());
                    updateDatabaseMetaViewVersion(androidDatabase, androidDatabase.getViewsVersion());
                } else {
                    androidDatabase.getSqLiteDatabase().setVersion(androidDatabase.getVersion());
                    updateDatabaseMetaViewVersion(androidDatabase, androidDatabase.getViewsVersion());
                }
            } else {
                // make sure database being attached are connected/opened
                for (AndroidDatabase otherDb : androidDatabase.getAttachedDatabases()) {
                    if (otherDb.isAttached()) {
                        throw new IllegalStateException("Attached databases cannot be attach type databases (for database [" + otherDb.getName() + "]");
                    }

                    connectDatabase(otherDb.getName());
                }

                // attached database
                attachDatabases(androidDatabase);
            }
        }
    }

    public void attachDatabases(AndroidDatabase db) {
        if (!db.isEncrypted()) {
            for (AndroidDatabase toDb : db.getAttachedDatabases()) {
                String sql = "ATTACH DATABASE '" + toDb.getPath() + "' AS " + toDb.getName();
                db.getSqLiteDatabase().execSQL(sql);
            }
        } else {
            for (AndroidDatabase toDb : db.getAttachedDatabases()) {
                String sql = "ATTACH DATABASE '" + toDb.getPath() + "' AS " + toDb.getName() + " KEY '" + toDb.getPassword() + "'";
                db.getSqLiteDatabase().execSQL(sql);
            }
        }
    }

    public void detachDatabases(String databaseName) {
        AndroidDatabase androidDatabase = databaseMap.get(databaseName);
        if (androidDatabase == null) {
            throw new IllegalArgumentException("Database [" + databaseName + "] does not exist");
        }

        detachDatabases(androidDatabase);
    }

    public void detachDatabases(AndroidDatabase db) {
        for (AndroidDatabase toDb : db.getAttachedDatabases()) {
            detachDatabase(db, toDb.getName());
        }
    }

    public void detachDatabase(String databaseName, String databaseToDetach) {
        AndroidDatabase androidDatabase = databaseMap.get(databaseName);
        if (androidDatabase == null) {
            throw new IllegalArgumentException("Database [" + databaseName + "] does not exist");
        }

        detachDatabase(androidDatabase, databaseToDetach);
    }

    public void detachDatabase(AndroidDatabase db, String databaseToDetach) {
        String sql = "DETACH DATABASE '" + databaseToDetach + "'";
        if (!db.isEncrypted()) {
            db.getSqLiteDatabase().execSQL(sql);
        } else {
            db.getSecureSqLiteDatabase().execSQL(sql);
        }
    }

    public void cleanDatabase(String databaseName) {
        onCleanDatabase(getDatabase(databaseName));
    }

    public void cleanAllDatabases() {
        for (AndroidDatabase androidDatabase : databaseMap.values()) {
            onCleanDatabase(androidDatabase);
        }
    }

    /**
     * Get Database File for an Internal Memory Database
     * @param databaseName Name of database file
     * @return Database File
     */
    public File getDatabaseFile(Context context, String databaseName) {
        File file =  context.getDatabasePath(databaseName);

        File directory = new File(file.getParent());

        // Make the necessary directories if needed.
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Cannot write to database path: " + file.getAbsolutePath());
        }

        return file;
    }

    /**
     * Get Database File for an Internal Memory Database
     * @param context Android Context
     * @param databaseSubDir Name of directory to place database in
     * @param databaseName Name of database file
     * @return Database File
     */
    public File getDatabaseExternalFile(Context context, String databaseSubDir, String databaseName) {
        boolean mediaMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        File cacheDir = context.getExternalFilesDir(databaseSubDir);

        File file;
        File directory;
        if (mediaMounted) {
            directory = cacheDir;
            file = new File(directory, databaseName);
        } else {
            directory = context.getFilesDir();
            file = new File(directory.getAbsolutePath() + File.separator + databaseName);
        }

        directory = new File(file.getParent());

        // Make the necessary directories if needed.
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Cannot write to SDCard.  Be sure the application has permissions.");
        }

        return file;
    }

    /**
     * Delete and then recreate the database
     * @param androidDatabase database to clean
     */
    public void onCleanDatabase(AndroidDatabase androidDatabase) {
        Log.i(TAG, "Cleaning Database");
        deleteDatabase(androidDatabase);
        connectDatabase(androidDatabase.getName(), false);  // do not update here, because it will cause a recursive call
    }

    /**
     * Delete databases and reset references from AndroidDatabaseManager
     */
    public void wipeDatabases() {
        Log.e(TAG, "Wiping databases");
        for (AndroidDatabase database : getDatabases()) {
            deleteDatabase(database);
        }

        // remove existing references
        // this should be done after clearing the preferences (to be sure not to use the old password)
        reset();
        identifyDatabases();
    }

    public void deleteDatabase(AndroidDatabase androidDatabase) {
        String databasePath = androidDatabase.getPath();

        try {
            if (!androidDatabase.inTransaction()) {
                androidDatabase.close(); // this will hang if there is an open transaction
            }
        } catch (Exception e) {
            // inTransaction can throw "IllegalStateException: attempt to re-open an already-closed object"
            // This should not keep LDS Tools from performing a SYNC
            Log.w(TAG, "deleteDatabase().inTransaction() Error: [" + e.getMessage() + "]");
        }

        Log.i(TAG, "Deleting database: [" + databasePath + "]");
        File databaseFile = new File(databasePath);
        if (databaseFile.exists() && !databaseFile.delete()) {
            String errorMessage = "FAILED to delete database: [" + databasePath + "]";
            Log.e(TAG, errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    public boolean deleteDatabaseFiles(AndroidDatabase db) {
        return deleteDatabaseFiles(new File(db.getPath()));
    }

    /**
     * Deletes a database including its journal file and other auxiliary files
     * that may have been created by the database engine.
     *
     * @param file The database file path.
     * @return True if the database was successfully deleted.
     */
    public boolean deleteDatabaseFiles(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }

        boolean deleted = false;
        deleted |= file.delete();
        deleted |= new File(file.getPath() + "-journal").delete();
        deleted |= new File(file.getPath() + "-shm").delete();
        deleted |= new File(file.getPath() + "-wal").delete();

        File dir = file.getParentFile();
        if (dir != null) {
            final String prefix = file.getName() + "-mj";
            final FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File candidate) {
                    return candidate.getName().startsWith(prefix);
                }
            };
            for (File masterJournal : dir.listFiles(filter)) {
                deleted |= masterJournal.delete();
            }
        }
        return deleted;
    }

    public boolean renameDatabaseFiles(File srcFile, File targetFile) {
        if (srcFile == null) {
            throw new IllegalArgumentException("file must not be null");
        }

        boolean renamed = false;
        renamed |= srcFile.renameTo(new File(targetFile.getPath()));
        renamed |= new File(srcFile.getPath() + "-journal").renameTo(new File(targetFile.getPath() + "-journal"));
        renamed |= new File(srcFile.getPath() + "-shm").renameTo(new File(targetFile.getPath() + "-shm"));
        renamed |= new File(srcFile.getPath() + "-wal").renameTo(new File(targetFile.getPath() + "-wal"));

        // delete srcFile -mj files
        File dir = srcFile.getParentFile();
        if (dir != null) {
            final String prefix = srcFile.getName() + "-mj";
            final FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File candidate) {
                    return candidate.getName().startsWith(prefix);
                }
            };
            for (File masterJournal : dir.listFiles(filter)) {
                renamed |= masterJournal.delete();
            }
        }
        return renamed;
    }

    public void beginTransaction(String databaseName) {
        getDatabase(databaseName).beginTransaction();
    }

    public void endTransaction(String databaseName, boolean success) {
        getDatabase(databaseName).endTransaction(success);
    }

    public void onUpgradeViews(AndroidDatabase androidDatabase, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database VIEWS [" + androidDatabase.getName() + "] from version " + oldVersion + " to " + newVersion);

        if (oldVersion != newVersion) {
            onDropViews(androidDatabase);
            onCreateViews(androidDatabase);
        }
    }

    public void createMetaTableIfNotExists(AndroidDatabase androidDatabase) {
        if (!androidDatabase.isEncrypted()) {
            if (!AndroidBaseManager.tableExists(androidDatabase, DBToolsMetaData.TABLE)) {
                AndroidBaseManager.executeSql(androidDatabase, DBToolsMetaData.CREATE_TABLE);
            }
        } else {
            if (!org.dbtools.android.domain.secure.AndroidBaseManager.tableExists(androidDatabase, DBToolsMetaData.TABLE)) {
                org.dbtools.android.domain.secure.AndroidBaseManager.executeSql(androidDatabase, DBToolsMetaData.CREATE_TABLE);
            }
        }
    }

    private String getViewVersionKey(AndroidDatabase androidDatabase) {
        return androidDatabase.getName() + "_view_version";
    }

    private void updateDatabaseMetaViewVersion(AndroidDatabase androidDatabase, int version) {
        createMetaTableIfNotExists(androidDatabase);

        int currentVersion = findViewVersion(androidDatabase);
        String keyName = getViewVersionKey(androidDatabase);

        String table = DBToolsMetaData.TABLE;

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBToolsMetaData.C_KEY, keyName);
        contentValues.put(DBToolsMetaData.C_VALUE, version);

        boolean encrypted = androidDatabase.isEncrypted();
        if (currentVersion != -1) {
            String selection = DBToolsMetaData.KEY_SELECTION;
            String[] whereArgs = new String[]{keyName};
            if (!encrypted) {
                androidDatabase.getSqLiteDatabase().update(table, contentValues, selection, whereArgs);
            } else {
                androidDatabase.getSecureSqLiteDatabase().update(table, contentValues, selection, whereArgs);
            }
        } else {
            if (!encrypted) {
                androidDatabase.getSqLiteDatabase().insert(table, null, contentValues);
            } else {
                androidDatabase.getSecureSqLiteDatabase().insert(table, null, contentValues);
            }
        }
    }

    private static final String FIND_VERSION = "SELECT " + DBToolsMetaData.C_VALUE + " FROM " + DBToolsMetaData.TABLE +
            " WHERE " + DBToolsMetaData.KEY_SELECTION;

    public int findViewVersion(AndroidDatabase androidDatabase) {
        createMetaTableIfNotExists(androidDatabase);

        int version;
        String[] selectionArgs = new String[]{getViewVersionKey(androidDatabase)};

        if (!androidDatabase.isEncrypted()) {
            version = (int) AndroidBaseManager.findLongByRawQuery(androidDatabase.getSqLiteDatabase(), FIND_VERSION, selectionArgs);
        } else {
            version = (int) org.dbtools.android.domain.secure.AndroidBaseManager.findLongByRawQuery(androidDatabase.getSecureSqLiteDatabase(), FIND_VERSION, selectionArgs);
        }

        return version;
    }
}
