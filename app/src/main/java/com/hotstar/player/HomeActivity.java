package com.hotstar.player;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hotstar.player.adapter.MenuLazyAdapter;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.manager.OverlayAdResourceManager;
import com.hotstar.player.events.BackToBaseFragmentEvent;
import com.hotstar.player.events.BackToExtendFragmentEvent;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.ConnectorErrEvent;
import com.hotstar.player.events.GoToDetailFragmentEvent;
import com.hotstar.player.events.GoToExtendFragmentEvent;
import com.hotstar.player.events.JumpBackBaseFragmentEvent;
import com.hotstar.player.events.JumpToDetailFragmentEvent;
import com.hotstar.player.events.PlayerFailureEvent;
import com.hotstar.player.fragments.BaseFragment;
import com.hotstar.player.fragments.DetailFragment;
import com.hotstar.player.fragments.ExtendFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.squareup.otto.Subscribe;

import java.util.Collection;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class HomeActivity extends ActionBarActivity {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + HomeActivity.class.getSimpleName();

    public static BaseFragment m_BaseFragment = null;
    public static ExtendFragment m_ExtendFragment = null;
    public static DetailFragment m_DetailFragment = null;

    // slide menu content
    private ListView listMenu;
    private MenuLazyAdapter menuAdapter;
    SlidingMenu slidingMenu;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (savedInstanceState != null)
            return;

        setContentView(R.layout.activity_home);

        AdVideoApplication.logger.i(LOG_TAG + "#onCreate", "savedInstanceState = " + savedInstanceState);

        // create left slide menu
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setShadowWidthRes(R.dimen.slidemenu_shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidemenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.slide_menu_content);

        // create base fragement for containLayout.
        //
        // Here can be BACK-Blank Activity issue : Need to check this
        //
        m_BaseFragment = new BaseFragment();
        getSupportFragmentManager ().beginTransaction ().replace(R.id.base_fragmentspace, m_BaseFragment).addToBackStack(BaseFragment.class.getSimpleName()).commit();
        getSupportFragmentManager().executePendingTransactions();
        getSupportFragmentManager().addOnBackStackChangedListener(backStackChangedListener);

        // set left slide menu event handler
        listMenu = (ListView) findViewById (R.id.listMenu);
        listMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onClickMenuList(position);
            }
        });
        setMenuItems();
        createOverlayAdDownloader();

        BusProvider.get().register(this);
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void onClickMenuList (int position)
    {
        if (position == 2) {
            confirmSignOut();
        }

        menuAdapter.setCurrentPosition(position);
        onSectionAttached(position);
    }

    protected void onResume ()
    {
        super.onResume();
    }

    protected void onDestroy() {
        super.onDestroy();

        BusProvider.get().unregister(this);
    }

    protected void onPause() {
        super.onPause();
    }

    public void showSlidingMenu() {
        slidingMenu.showMenu();
    }

    private void setMenuItems ()
    {
        menuAdapter = new MenuLazyAdapter(this);
        /*
        menuAdapter.addItem ("Hot Now!");
        menuAdapter.addItem ("My Playlist");
        menuAdapter.addItem ("Downloads");
        menuAdapter.addItem ("Shows");
        menuAdapter.addItem ("Movies");
        menuAdapter.addItem ("Sports");
        menuAdapter.addItem ("Collections");
        menuAdapter.addItem("International");
        */
        /*
        menuAdapter.addItem("Featured");
         */
        menuAdapter.addItem("TV Shows");
        menuAdapter.addItem("Movies");

        if (HotStarApplication.getInstance().getLoginStatus() != HotStarApplication.UserStatusType.STATUS_USER_LOGIN_ANONYMOUS)
            menuAdapter.addItem(HotStarApplication.getInstance().getUsername() + " - Sign Out");

        //call this last!
        menuAdapter.AddedAllItems ();
        listMenu.setAdapter(menuAdapter);
    }

    private void onSectionAttached (int number)
    {
        if(slidingMenu.isMenuShowing())
            slidingMenu.showContent();

        /*
        if ((number == 1) || (number == 2))
        {
            // ignore "My Playlist" & "Downloads"
        }
        else {
            if (number > 2) number -= 2;
            if (m_BaseFragment != null)
                m_BaseFragment.setFragmentStatus(number);
        }
        */

        m_BaseFragment.setFragmentStatus(number);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * From BaseFragment To ExtendFragment (via user action)
     *
     * @param event
     */
    @Subscribe
    public void onGotoExtendFragmentEvent(GoToExtendFragmentEvent event) {
        AdVideoApplication.logger.i(LOG_TAG + "#onGotoExtendFragmentEvent", "GO-TO ExtendFragment");

        m_ExtendFragment = new ExtendFragment();
        m_ExtendFragment.setFragmentType(event.getModel());
        getSupportFragmentManager ().beginTransaction ().replace(R.id.base_fragmentspace, m_ExtendFragment).addToBackStack(ExtendFragment.class.getSimpleName()).commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    /**
     * From BaseFragment To DetailFragment (via user action)
     *
     * @param event
     */
    @Subscribe
    public void onJumpToDetailFragmentEvent(JumpToDetailFragmentEvent event){
        AdVideoApplication.logger.i(LOG_TAG + "#onJumpToDetailFragmentEvent", "JUMP-TO DetailFragment");

        m_DetailFragment = new DetailFragment();
        m_DetailFragment.setVideoItem(event.getModel().getVideoItem());
        getSupportFragmentManager ().beginTransaction ().replace(R.id.base_fragmentspace, m_DetailFragment).addToBackStack(DetailFragment.class.getSimpleName()).commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    /**
     * From DetailFragment To ExtendFragment (via user action)
     *
     * @param event
     */

    @Subscribe
    public void onBackToExtendFragmentEvent(BackToExtendFragmentEvent event) {
        AdVideoApplication.logger.i(LOG_TAG + "#onBackToExtendFragmentEvent", "BACK-TO ExtendFragment");

        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().executePendingTransactions();
    }

    /**
     * From DetailFragment To HomeFragment (via user action)
     *
     * @param event
     */
    @Subscribe
    public void onJumpBackBaseFragmentEvent(JumpBackBaseFragmentEvent event) {
        AdVideoApplication.logger.i(LOG_TAG + "#onJumpBackBaseFragmentEvent", "JUMP-BACK BaseFragment");

        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().executePendingTransactions();
    }

    /**
     * From ExtendFragment To DetailFragment (via user action)
     *
     * @param event
     */
    @Subscribe
    public void onGotoDetailFragmentEvent(GoToDetailFragmentEvent event) {
        AdVideoApplication.logger.i(LOG_TAG + "#onGotoDetailFragmentEvent", "GO-TO DetailFragment");

        m_DetailFragment = new DetailFragment();
        m_DetailFragment.setVideoItem(event.getModel().getVideoItem());
        getSupportFragmentManager ().beginTransaction ().replace(R.id.base_fragmentspace, m_DetailFragment).addToBackStack(DetailFragment.class.getSimpleName()).commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    /**
     * From ExtendFragment To BaseFragment ( via user action)
     *
     * @param event
     */
    @Subscribe
    public void onBackToBaseFragmentEvent(BackToBaseFragmentEvent event) {
        AdVideoApplication.logger.i(LOG_TAG + "#onBackToBaseFragmentEvent", "BACK-TO BaseFragment");

        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().executePendingTransactions();
    }

    /**
     * Connector Error Event occured
     *
     * @param event
     */
    @Subscribe
    public void onConnectorErrEvent(ConnectorErrEvent event) {

    }

    private final FragmentManager.OnBackStackChangedListener backStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {

        }
    };

    @Override
    public void onBackPressed () {
        final FragmentManager fm = getSupportFragmentManager ();
        if (fm.getBackStackEntryCount () > 1) {
            getSupportFragmentManager ().popBackStack ();
        }
        else {
            if (HotStarApplication.getInstance().getLoginStatus() == HotStarApplication.UserStatusType.STATUS_USER_LOGIN_ANONYMOUS)
                finish();
            else
                confirmSignOut();
        }

    }

    private void confirmSignOut() {
        AlertDialog.Builder dialog = new AlertDialog.Builder (this);
        dialog.setTitle ("Information");
        dialog.setMessage ("Are you sure to sign out?");
        dialog.setPositiveButton ("Yes", new DialogInterface.OnClickListener () {
            @Override
            public void onClick (DialogInterface dialog, int which) {
                HotStarApplication.getInstance().logout();
                finish();
            }
        });
        dialog.setNegativeButton ("No", new DialogInterface.OnClickListener () {
            @Override
            public void onClick (DialogInterface dialog, int which) {
            }
        });
        dialog.show ();
    }

    void createOverlayAdDownloader() {
        OverlayAdResourceManager.createInstance(this);
    }
}
