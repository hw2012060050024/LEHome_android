package my.home.lehome.fragment;

import de.greenrobot.lehome.Shortcut;
import my.home.lehome.R;
import my.home.lehome.activity.MainActivity;
import my.home.lehome.adapter.ShortcutArrayAdapter;
import my.home.lehome.asynctask.SendCommandAsyncTask;
import my.home.lehome.helper.DBHelper;
import my.home.lehome.service.ConnectionService;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ShortcutFragment extends ListFragment {
	
	private ShortcutArrayAdapter adapter;
	
    @Override
    public View onCreateView(LayoutInflater inflater,      
    		ViewGroup container, Bundle savedInstanceState) {             
        View rootView =  inflater.inflate(R.layout.shortcut_fragment, container, false); 
        if (adapter == null) {
        	adapter = new ShortcutArrayAdapter(getActivity(), R.layout.shortcut_item);
        	adapter.setData(DBHelper.getAllShortcuts());
		}
        setListAdapter(adapter);
        return rootView;
    }
          
    @Override
    public void onCreate(Bundle savedInstanceState) {     
        super.onCreate(savedInstanceState);
    }     
    
    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
		registerForContextMenu(getListView());
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == getListView().getId()) {
	        MenuInflater inflater = getActivity().getMenuInflater();
	        inflater.inflate(R.menu.delete_shortcut_item, menu);
		}
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
          AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
          switch(item.getItemId()) {
              case R.id.delete_shortcut_item:
            	  Shortcut shortcut = adapter.getItem(info.position);
            	  adapter.remove(shortcut);
            	  adapter.notifyDataSetChanged();
            	  DBHelper.deleteShortcut(shortcut.getId());
                  return true;
              default:
                    return super.onContextItemSelected(item);
          }
    }
    
    @Override
    public void onListItemClick(ListView parent, View v,      
    int position, long id)      
    {
    	Shortcut shortcut = adapter.getItem(position);
    	shortcut.setInvoke_count(shortcut.getInvoke_count() + 1);
    	DBHelper.updateShortcut(shortcut);
    	
        MainActivity mainActivity = (MainActivity) getActivity();
		new SendCommandAsyncTask(mainActivity).execute(shortcut.getContent());

        Toast.makeText(getActivity(),      
            getResources().getString(R.string.com_exec) + ":" + shortcut.getContent(),      
            Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
        switch (id) {
		case R.id.add_shortcut_item:
			addListItemWithUserInput();
        	return true;
		default:
			break;
		}
    	return super.onOptionsItemSelected(item);
    }
    
    private void addListItemWithUserInput() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

//    	alert.setTitle(getResources().getString(R.string.add_shortcut_item));
    	alert.setMessage(getResources().getString(R.string.add_shortcut_item_summ));

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(getActivity());
    	input.setSingleLine(true);
    	input.setHint(R.string.qs_default_input);
    	alert.setView(input);

    	alert.setPositiveButton(getResources().getString(R.string.com_comfirm)
    							, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		String value = input.getText().toString();
	    	  	if (value != null && !value.trim().equals("")) {
	    	  		addShortcut(value);
				}
	    	}
    	});

    	alert.setNegativeButton(getResources().getString(R.string.com_cancel), 
    							new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			// Canceled.
    		}
    	});

    	alert.show();
    	// see http://androidsnippets.com/prompt-user-input-with-an-alertdialog
	}
    
    public boolean addShortcut(String content) {
    	if (content == null || content.trim() == "") {
    		return false;
		}
    	Shortcut shortcut = new Shortcut();
    	shortcut.setContent(content);
    	shortcut.setInvoke_count(0);
    	shortcut.setWeight(1.0);
		this.adapter.add(shortcut);
		DBHelper.addShortcut(shortcut);
		return true;
	}
}
