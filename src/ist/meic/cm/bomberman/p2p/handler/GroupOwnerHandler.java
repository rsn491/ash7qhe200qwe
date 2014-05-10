package ist.meic.cm.bomberman.p2p.handler;

import android.os.Handler;
import android.util.Log;

import ist.meic.cm.bomberman.p2p.WiFiServiceDiscoveryActivity;
import ist.meic.cm.bomberman.p2p.manager.Manager;
import ist.meic.cm.bomberman.p2p.manager.WiFiGlobal;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The implementation of a ServerSocket handler. This is used by the wifi p2p
 * group owner.
 */
public class GroupOwnerHandler extends Thread {

	ServerSocket socket = null;
	private final int THREAD_COUNT = 4;
	private String prefs;
	private String playerName;
	private static final String TAG = "GroupOwnerSocketHandler";

	public GroupOwnerHandler(String playerName, String prefs)
			throws IOException {
		try {
			socket = new ServerSocket(WiFiServiceDiscoveryActivity.SERVER_PORT);
			socket.setReuseAddress(true);
			Log.d("GroupOwnerSocketHandler", "Socket Started");
		} catch (IOException e) {
			e.printStackTrace();
			pool.shutdownNow();
			throw e;
		}

		WiFiGlobal global = WiFiGlobal.getInstance();
		global.setServerSocket(socket);
		this.prefs = prefs;
		this.playerName = playerName;
	}

	/**
	 * A ThreadPool for client sockets.
	 */
	private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
			THREAD_COUNT, THREAD_COUNT, 10000, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>());
	private Manager manager;
	private boolean running;

	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				// A blocking operation. Initiate a ChatManager instance when
				// there is a new connection

				pool.execute(manager = new Manager(playerName, socket.accept(), prefs));
				Log.d(TAG, "Launching the I/O handler");

			} catch (IOException e) {
				/*
				 * try { if (socket != null && !socket.isClosed())
				 * socket.close(); } catch (IOException ioe) {
				 * 
				 * }
				 */
				// e.printStackTrace();
				pool.shutdownNow();
				break;
			}
		}
	}

	public void setRunning() {
		this.running = false;
		pool.shutdownNow();
	}

	public Manager getManager() {

		return manager;
	}

}
