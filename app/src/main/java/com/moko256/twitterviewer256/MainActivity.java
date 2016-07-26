package com.moko256.twitterviewer256;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.TwitterException;
import twitter4j.User;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (Static.twitter == null) {
            finish();
            startActivity(new Intent(this, OAuthActivity.class));
            return;
        }

        setTheme(R.style.MainActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Observable
                .create(subscriber->{
                    try {
                        subscriber.onNext(Static.twitter.verifyCredentials());
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            User user=(User) result;
                            Static.user=user;

                            ((TextView)findViewById(R.id.user_name)).setText(user.getName());
                            ((TextView)findViewById(R.id.user_id)).setText(Static.plusAtMark(user.getScreenName()));

                            ImageView userImage=(ImageView)findViewById(R.id.user_image);
                            ImageView userBackgroundImage=(ImageView)findViewById(R.id.user_bg_image);

                            userImage.setOnClickListener(v -> startMyUserActivity());
                            userBackgroundImage.setOnClickListener(v -> startMyUserActivity());

                            Glide.with(MainActivity.this).load(user.getBiggerProfileImageURL()).into(userImage);
                            Glide.with(MainActivity.this).load(user.getProfileBannerRetinaURL()).into(userBackgroundImage);
                        },
                        Throwable::printStackTrace,
                        ()->{}
                );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item-> {
            int id = item.getItemId();

            if(!item.isChecked()){
                switch(id){
                    case R.id.nav_TL:
                        startFragment(new TimeLineFragment());
                        break;
                    case R.id.nav_account:
                        startMyUserActivity();
                        break;
                    case R.id.nav_follow_and_follower:
                        startFragment(new MyFollowFollowerFragment());
                        break;
                    case R.id.nav_like:
                        startFragment(new LikeMeFragment());
                        break;
                    case R.id.nav_settings:
                        startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                        break;
                }
            }

            drawer.closeDrawer(GravityCompat.START);

            return (id != R.id.nav_settings)&&(id != R.id.nav_account);

        });

        findViewById(R.id.fab).setOnClickListener(v -> startActivity(new Intent(MainActivity.this , SendTweetActivity.class)));

        TabLayout tabLayout=(TabLayout) findViewById(R.id.toolbar_tab);

        getSupportFragmentManager()
                .addOnBackStackChangedListener(() -> onFragmentBackStackChanged(actionBar,navigationView,tabLayout));

        if(savedInstanceState==null){
            startFragment(new TimeLineFragment());
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onFragmentBackStackChanged(getSupportActionBar(),(NavigationView) findViewById(R.id.nav_view),(TabLayout)findViewById(R.id.toolbar_tab));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    private void startMyUserActivity() {
        if (Static.user!=null){
            startActivity(new Intent(MainActivity.this, ShowUserActivity.class).putExtra("user", Static.user));
        }
    }

    private void startFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainLayout, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void onFragmentBackStackChanged(ActionBar actionBar, NavigationView navigationView, TabLayout tabLayout){
        Fragment fragment=getSupportFragmentManager()
                .findFragmentById(R.id.mainLayout);
        if (fragment != null) {
            if(fragment instanceof ToolbarTitleInterface && fragment instanceof NavigationPositionInterface){
                actionBar.setTitle(((ToolbarTitleInterface)fragment).getTitleResourceId());
                navigationView.setCheckedItem(((NavigationPositionInterface)fragment).getNavigationPosition());
            }

            if(fragment instanceof UseTabsInterface) {
                tabLayout.setVisibility(View.VISIBLE);
                tabLayout.setupWithViewPager(((UseTabsInterface)fragment).getTabsViewPager());
            }
            else{
                tabLayout.setVisibility(View.GONE);
            }
        }
        else {
            finish();
        }
    }

}