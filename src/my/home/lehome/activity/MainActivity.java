package my.home.lehome.activity;

import my.home.lehome.R;
import my.home.lehome.fragment.ChatFragment;
import my.home.lehome.fragment.NavigationDrawerFragment;
import my.home.lehome.fragment.ShortcutFragment;
import my.home.lehome.helper.DBHelper;
import my.home.lehome.helper.NetworkHelper;
import my.home.lehome.service.ConnectionService;
import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	public static boolean STOPPED = false;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private int mCurrentSection;
    private boolean doubleBackToExitPressedOnce;
    
    private ChatFragment chatFragment;
    private ShortcutFragment shortcurFragment;
    private ConnectionService connectionService;
    private ServiceConnection connection = new ServiceConnection() {  
		  
        @Override  
        public void onServiceDisconnected(ComponentName name) {  
        }  
  
        @Override  
        public void onServiceConnected(ComponentName name, IBinder service) {
        	connectionService = ((ConnectionService.LocalBinder) service).getService();
        }  
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        this.setupService();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        
        mTitle = getTitle();
        
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }
    
    private void setupService() {
    	DBHelper.initHelper(this);
    	startService(new Intent(this, ConnectionService.class));
    	this.bindService(new Intent(this, ConnectionService.class), connection, Context.BIND_AUTO_CREATE);
    	STOPPED = false;
	}
    
    @Override
    protected void onDestroy() {
    	this.unbindService(connection);
    	super.onDestroy();
    };
    
    @Override
    protected void onResume() {
      super.onResume();
      ConnectionService.activityResumed();
    }

    @Override
    protected void onPause() {
      super.onPause();
      ConnectionService.activityPaused();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    	Fragment fragment = null;
    	switch (position) {
		case 0:
	    	if(chatFragment == null) {
	    		chatFragment = new ChatFragment();
	    	}
	    	fragment = chatFragment;
//			fragment = new PagerFragment();
			break;
		case 1:
	    	if(shortcurFragment == null) {
	    		shortcurFragment = new ShortcutFragment();
	    	}
	    	fragment = shortcurFragment;
			break;

		default:
			break;
		}
    	this.onSectionAttached(position);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
    	mCurrentSection = number;
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_section1);
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
        	switch (mCurrentSection) {
			case 0:
				getMenuInflater().inflate(R.menu.main, menu);
				break;
			case 1:
				getMenuInflater().inflate(R.menu.shortcut, menu);
				break;
			default:
				break;
			}
            
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
		case R.id.action_settings:
			Intent intent = new Intent();
        	intent.setClass(MainActivity.this, SettingsActivity.class);
        	startActivityForResult(intent, 0); 
			// The if returned true the click event will be consumed by the
			// onOptionsItemSelect() call and won't fall through to other item 
			// click functions. If your return false it may check the ID of 
			// the event in other item selection functions.
        	 return true;
		case R.id.local_ip_item:
			String ipString = NetworkHelper.getIPAddress(true);
			Toast.makeText(this, getResources().getString(R.string.local_ip_item) + ":" + ipString, Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_exit:
			stopService(new Intent(this, ConnectionService.class));
			this.finish();
			STOPPED = true;
			return true;
		default:
			break;
		}
        return super.onOptionsItemSelected(item);
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	String old_sub_address = ConnectionService.SUBSCRIBE_ADDRESS;
    	String old_pub_address = ConnectionService.PUBLISH_ADDRESS;
    	loadPref();
    	if (!old_sub_address.equals(ConnectionService.SUBSCRIBE_ADDRESS)
    			|| !old_pub_address.equals(ConnectionService.PUBLISH_ADDRESS)) {
    		Intent bindIntent = new Intent(this, ConnectionService.class);  
        	this.unbindService(connection);
        	stopService(new Intent(this, ConnectionService.class));
        	startService(new Intent(this, ConnectionService.class));
        	this.bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
		}
//    	MessageHelper.sendServerMsgToList(getResources().getString(R.string.pref_sub_address) + ":" + ConnectionService.SUBSCRIBE_ADDRESS);
//    	MessageHelper.sendServerMsgToList(getResources().getString(R.string.pref_pub_address) + ":" + ConnectionService.PUBLISH_ADDRESS);
    }
    
    
       
    private void loadPref(){
    	connectionService.loadPref();
    }
    
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.double_back_to_quit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;                       
            }
        }, 2000);
    }

	public ConnectionService getConnectionService() {
		return connectionService;
	}

	public ChatFragment getChatFragment() {
		return chatFragment;
	}

	public ShortcutFragment getShortcurFragment() {
		return shortcurFragment;
	}
	
}
