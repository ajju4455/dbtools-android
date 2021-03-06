package org.dbtools.android.domain.database;


import android.content.ContentValues;
import android.database.Cursor;
import android.util.Pair;
import org.sqlite.database.DatabaseErrorHandler;
import org.sqlite.database.SQLException;
import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteStatement;
import org.sqlite.database.sqlite.SQLiteTransactionListener;
import org.sqlite.os.CancellationSignal;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SQLiteDatabaseWrapper implements DatabaseWrapper<SQLiteDatabase> {
    private SQLiteDatabase database;

    private static boolean libraryLoaded = false;

    /**
     * Create a standard version of the database
     */
    public SQLiteDatabaseWrapper(String path) {
        loadLibrary();
        database = SQLiteDatabase.openOrCreateDatabase(path, null);
    }

    /**
     * Create a secure/encrypted version of the database
     */
    public SQLiteDatabaseWrapper(String path, String password) {
        loadLibrary();
        database = SQLiteDatabase.openOrCreateDatabase(path, null);
        database.execSQL("PRAGMA key = '" + password + "'");
    }

    private void loadLibrary() {
        if (!isLibraryLoaded()) {
            System.loadLibrary("sqliteX"); // load the sqlite.org library
            setLibraryLoaded(true);
        }
    }

    private static void setLibraryLoaded(boolean b) {
        libraryLoaded = b;
    }

    private static boolean isLibraryLoaded() {
        return libraryLoaded;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public void attachDatabase(String toDbPath, String toDbName, String toDbPassword) {
        String sql = "ATTACH DATABASE '" + toDbPath + "' AS " + toDbName;
        database.execSQL(sql);
    }

    // **** following are from SQLiteDatabase.java ****

    @Override
    public void close() {
        database.close();
    }

    public static int releaseMemory() {
        return SQLiteDatabase.releaseMemory();
    }

    public void beginTransactionNonExclusive() {
        database.beginTransactionNonExclusive();
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        database.execSQL(sql, bindArgs);
    }

    public void addCustomFunction(String name, int numArgs, SQLiteDatabase.CustomFunction function) {
        database.addCustomFunction(name, numArgs, function);
    }

    public void setMaxSqlCacheSize(int cacheSize) {
        database.setMaxSqlCacheSize(cacheSize);
    }

    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, CancellationSignal cancellationSignal) {
        return database.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal);
    }

    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
        return database.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
    }

    public String getPath() {
        return database.getPath();
    }

    public boolean yieldIfContendedSafely(long sleepAfterYieldDelay) {
        return database.yieldIfContendedSafely(sleepAfterYieldDelay);
    }

    public static boolean hasCodec() {
        return SQLiteDatabase.hasCodec();
    }

    public void acquireReference() {
        database.acquireReference();
    }

    public static SQLiteDatabase openOrCreateDatabase(String path, SQLiteDatabase.CursorFactory factory) {
        return SQLiteDatabase.openOrCreateDatabase(path, factory);
    }

    public static SQLiteDatabase openOrCreateDatabase(String path, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return SQLiteDatabase.openOrCreateDatabase(path, factory, errorHandler);
    }

    public static boolean deleteDatabase(File file) {
        return SQLiteDatabase.deleteDatabase(file);
    }

    public long setMaximumSize(long numBytes) {
        return database.setMaximumSize(numBytes);
    }

    public boolean isInMemoryDatabase() {
        return database.isInMemoryDatabase();
    }

    public boolean yieldIfContendedSafely() {
        return database.yieldIfContendedSafely();
    }

    @Deprecated
    public Map<String, String> getSyncedTables() {
        return database.getSyncedTables();
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public void beginTransactionWithListenerNonExclusive(SQLiteTransactionListener transactionListener) {
        database.beginTransactionWithListenerNonExclusive(transactionListener);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs, CancellationSignal cancellationSignal) {
        return database.rawQuery(sql, selectionArgs, cancellationSignal);
    }

    public boolean isReadOnly() {
        return database.isReadOnly();
    }

    public boolean enableWriteAheadLogging() {
        return database.enableWriteAheadLogging();
    }

    @Deprecated
    public boolean isDbLockedByOtherThreads() {
        return database.isDbLockedByOtherThreads();
    }

    public Cursor queryWithFactory(SQLiteDatabase.CursorFactory cursorFactory, boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return database.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public long getMaximumSize() {
        return database.getMaximumSize();
    }

    public static SQLiteDatabase create(SQLiteDatabase.CursorFactory factory) {
        return SQLiteDatabase.create(factory);
    }

    public boolean isWriteAheadLoggingEnabled() {
        return database.isWriteAheadLoggingEnabled();
    }

    @Deprecated
    public void markTableSyncable(String table, String foreignKey, String updateTable) {
        database.markTableSyncable(table, foreignKey, updateTable);
    }

    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
        return database.insertOrThrow(table, nullColumnHack, values);
    }

    public boolean needUpgrade(int newVersion) {
        return database.needUpgrade(newVersion);
    }

    public int updateWithOnConflict(String table, ContentValues values, String whereClause, String[] whereArgs, int conflictAlgorithm) {
        return database.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm);
    }

    public void releaseReference() {
        database.releaseReference();
    }

    public boolean isDatabaseIntegrityOk() {
        return database.isDatabaseIntegrityOk();
    }

    public long replace(String table, String nullColumnHack, ContentValues initialValues) {
        return database.replace(table, nullColumnHack, initialValues);
    }

    public void setPageSize(long numBytes) {
        database.setPageSize(numBytes);
    }

    public void beginTransactionWithListener(SQLiteTransactionListener transactionListener) {
        database.beginTransactionWithListener(transactionListener);
    }

    public void setForeignKeyConstraintsEnabled(boolean enable) {
        database.setForeignKeyConstraintsEnabled(enable);
    }

    public static SQLiteDatabase openDatabase(String path, SQLiteDatabase.CursorFactory factory, int flags) {
        return SQLiteDatabase.openDatabase(path, factory, flags);
    }

    public Cursor queryWithFactory(SQLiteDatabase.CursorFactory cursorFactory, boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, CancellationSignal cancellationSignal) {
        return database.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal);
    }

    public void disableWriteAheadLogging() {
        database.disableWriteAheadLogging();
    }

    public static String findEditTable(String tables) {
        return SQLiteDatabase.findEditTable(tables);
    }

    public void enableLocalizedCollators() {
        database.enableLocalizedCollators();
    }

    @Deprecated
    public void setLockingEnabled(boolean lockingEnabled) {
        database.setLockingEnabled(lockingEnabled);
    }

    public long replaceOrThrow(String table, String nullColumnHack, ContentValues initialValues) throws SQLException {
        return database.replaceOrThrow(table, nullColumnHack, initialValues);
    }

    public List<Pair<String, String>> getAttachedDbs() {
        return database.getAttachedDbs();
    }

    public SQLiteStatement compileStatement(String sql) throws SQLException {
        return database.compileStatement(sql);
    }

    public static SQLiteDatabase openOrCreateDatabase(File file, SQLiteDatabase.CursorFactory factory) {
        return SQLiteDatabase.openOrCreateDatabase(file, factory);
    }

    public Cursor rawQueryWithFactory(SQLiteDatabase.CursorFactory cursorFactory, String sql, String[] selectionArgs, String editTable) {
        return database.rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable);
    }

    public long getPageSize() {
        return database.getPageSize();
    }

    public boolean isDbLockedByCurrentThread() {
        return database.isDbLockedByCurrentThread();
    }

    public void setLocale(Locale locale) {
        database.setLocale(locale);
    }

    @Deprecated
    public boolean yieldIfContended() {
        return database.yieldIfContended();
    }

    public Cursor rawQueryWithFactory(SQLiteDatabase.CursorFactory cursorFactory, String sql, String[] selectionArgs, String editTable, CancellationSignal cancellationSignal) {
        return database.rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable, cancellationSignal);
    }

    @Deprecated
    public void releaseReferenceFromContainer() {
        database.releaseReferenceFromContainer();
    }

    public void reopenReadWrite() {
        database.reopenReadWrite();
    }

    public static SQLiteDatabase openDatabase(String path, SQLiteDatabase.CursorFactory factory, int flags, DatabaseErrorHandler errorHandler) {
        return SQLiteDatabase.openDatabase(path, factory, flags, errorHandler);
    }

    @Deprecated
    public void markTableSyncable(String table, String deletedTable) {
        database.markTableSyncable(table, deletedTable);
    }

    @Override
    public void beginTransaction() {
        database.beginTransaction();
    }

    @Override
    public void endTransaction() {
        database.endTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        database.setTransactionSuccessful();
    }

    @Override
    public boolean inTransaction() {
        return database.inTransaction();
    }

    @Override
    public int getVersion() {
        return database.getVersion();
    }

    @Override
    public void setVersion(int version) {
        database.setVersion(version);
    }

    @Override
    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return database.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return database.rawQuery(sql, selectionArgs);
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return database.insert(table, nullColumnHack, values);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return database.delete(table, whereClause, whereArgs);
    }

    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return database.update(table, values, whereClause, whereArgs);
    }

    @Override
    public void execSQL(String sql) throws SQLException {
        database.execSQL(sql);
    }

    @Override
    public boolean isOpen() {
        return database.isOpen();
    }

    @Override
    public String toString() {
        return database.toString();
    }
}
