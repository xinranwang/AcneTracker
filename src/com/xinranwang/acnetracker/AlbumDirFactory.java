package com.xinranwang.acnetracker;

import java.io.File;

import android.os.Environment;

public final class AlbumDirFactory {
	
	// Standard storage location for digital camera files
	private static final String CAMERA_DIR = "/dcim/";
	
	public File getAlbumStorageDir(String albumName) {
		return new File (
				Environment.getExternalStorageDirectory()
				+ CAMERA_DIR
				+ albumName
		);
	}

}
