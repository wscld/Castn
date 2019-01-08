package com.wslclds.castn.factory;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.realm.Realm;

public class DatabaseBackupManager {

    private String EXPORT_REALM_FILE_NAME = "castn.backup";
    private String IMPORT_REALM_FILE_NAME = "default.realm";

    private final static String TAG = DatabaseBackupManager.class.getName();

    private Realm realm;
    private Context context;


    public DatabaseBackupManager(Context context) {
        this.realm = new DatabaseManager(context).getRealmInstance();
        this.context = context;
    }

    public File getStorageFolder(){
        File mainDir = Environment.getExternalStorageDirectory();
        File castnFolder = new File(mainDir,"Castn");
        if(castnFolder.isDirectory()){
            return castnFolder;
        }else {
            castnFolder.mkdir();
            return castnFolder;
        }
    }

    public boolean backup() {
        File exportRealmFile;
        Log.d(TAG, "Realm DB Path = " + realm.getPath());
        try {
            getStorageFolder().mkdirs();
            // create a backup file
            exportRealmFile = new File(getStorageFolder(), EXPORT_REALM_FILE_NAME);
            // if backup file already exists, delete it
            exportRealmFile.delete();
            // copy current realm to backup file
            realm.writeCopyTo(exportRealmFile);

        } catch (Exception e) {
            return false;
        }

        String msg = "File exported to Path: " + getStorageFolder() + "/" + EXPORT_REALM_FILE_NAME;
        Log.d(TAG, msg);

        realm.close();

        return true;
    }

    public boolean restore() {
        //Restore
        try{
            String restoreFilePath = getStorageFolder() + "/" + EXPORT_REALM_FILE_NAME;
            Log.d(TAG, "oldFilePath = " + restoreFilePath);
            copyBundledRealmFile(restoreFilePath, IMPORT_REALM_FILE_NAME);
            Log.d(TAG, "Data restore is done");
        }catch (Exception e){
            return false;
        }

        return true;
    }

    private String copyBundledRealmFile(String oldFilePath, String outFileName) {
        try {
            File file = new File(context.getFilesDir(), outFileName);

            FileOutputStream outputStream = new FileOutputStream(file);

            FileInputStream inputStream = new FileInputStream(new File(oldFilePath));

            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String dbPath(){
        return realm.getPath();
    }
}