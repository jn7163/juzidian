package org.juzidian.android;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roboguice.activity.RoboActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;

public class MainActivity extends RoboActivity {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

	private DictionaryDownloadService downloadService;

	private final ServiceConnection downloadServiceConnection = new DownloadServiceConnection();

	private final DictionaryDownloadListener downloadListener = new DownloadListener();

	private boolean started;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		LOGGER.debug("creating main activity");
		super.onCreate(savedInstanceState);
		if (this.isDbInitialized()) {
			this.setContentView(R.layout.activity_main);
		} else {
			this.initializeDatabase();
			this.setContentView(R.layout.download_view);
		}
	}

	private boolean isDbInitialized() {
		return new File(DictionaryDownloadService.DICTIONARY_DB_PATH).exists();
	}

	private void initializeDatabase() {
		LOGGER.debug("Binding to download service");
		final Intent intent = new Intent(this, DictionaryDownloadService.class);
		this.bindService(intent, this.downloadServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void onDownloadServiceConnected(final DictionaryDownloadService downloadService) {
		LOGGER.debug("download service connected");
		this.downloadService = downloadService;
		synchronized (downloadService) {
			if (!downloadService.isDownloadInProgress()) {
				downloadService.downloadDictionary();
			} else {
				LOGGER.debug("dictionary download already in progress");
			}
			downloadService.addDownloadListener(this.downloadListener);
		}
	}

	public void onDownloadServiceDisconnected() {
		this.downloadService = null;
	}

	@Override
	protected void onStart() {
		LOGGER.debug("starting main activity");
		super.onStart();
		this.started = true;
	}

	@Override
	protected void onStop() {
		LOGGER.debug("stopping main activity");
		super.onStop();
		this.started = false;
	}

	@Override
	protected void onDestroy() {
		LOGGER.debug("destroying main activity");
		super.onDestroy();
		if (this.downloadService != null) {
			this.unbindService(this.downloadServiceConnection);
			this.downloadService.removeDownloadListener(this.downloadListener);
		}
	}

	private void onDownloadSuccess() {
		this.finish();
		if (this.started) {
			/* if activity was stopped then don't unobtrusively restart */
			this.startActivity(this.getIntent());
		}
	}

	private void onDownloadFailure() {
		this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private class DownloadListener implements DictionaryDownloadListener {

		@Override
		public void downloadSuccess() {
			MainActivity.this.onDownloadSuccess();
		}

		@Override
		public void downloadFailure() {
			MainActivity.this.onDownloadFailure();
		}

	}

	private class DownloadServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(final ComponentName name, final IBinder binder) {
			final DictionaryDownloadService downloadService = ((DictionaryDownloadService.Binder) binder).getService();
			MainActivity.this.onDownloadServiceConnected(downloadService);
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			MainActivity.this.onDownloadServiceDisconnected();
		}

	}

}
