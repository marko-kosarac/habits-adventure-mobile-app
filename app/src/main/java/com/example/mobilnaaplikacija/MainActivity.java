package com.example.mobilnaaplikacija;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mobilnaaplikacija.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private ActionBar actionBar;
    private AppBarConfiguration mAppBarConfiguration;
    private Set<Integer> topLevelDestinations = new HashSet<>();

    private ActionBarDrawerToggle actionBarDrawerToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        topLevelDestinations.add(R.id.action_settings);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Početni menu
        navigationView.inflateMenu(R.menu.logged_out_drawer);

        // Top-level destinacije (hamburger se prikazuje za ove)
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.mainFragment,   // dodaj mainFragment kao top-level
                R.id.nav_profile,
                R.id.profile_page,
                R.id.nav_register,
                R.id.statistics_page
        ).setOpenableLayout(drawer).build();

        // Poveži Toolbar i Drawer sa NavController-om
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Drawer toggle (opciono, ali sync-uje hamburger animaciju)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navController.navigate(R.id.mainFragment);
            } else if (id == R.id.nav_profile) {
                navController.navigate(R.id.profile_page);
            }
            else if (id == R.id.nav_statistics){
                navController.navigate((R.id.statistics_page));
            }else if (id == R.id.nav_logout) {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.logged_out_drawer);
                navController.navigate(R.id.homeFragment);
            }

            drawer.closeDrawers();
            return true;
        });
        hideSystemUI();
    }

    public void onLoginSuccess() {
        // Zamena menu-a sa main drawer
        binding.navView.getMenu().clear();
        binding.navView.inflateMenu(R.menu.main_drawer);

        // Navigacija na HomePageFragment
        navController.navigate(R.id.mainFragment,
                null,
                new androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.homeFragment, true)
                        .build()
        );
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // menu.clear();
        // koristimo ako je nasa arhitekrura takva da imamo jednu aktivnost
        // i vise fragmentaa gde svaki od njih ima svoj menu unutar toolbar-a

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void setMainDrawer() {
        NavigationView navView = findViewById(R.id.nav_view);
        navView.getMenu().clear();
        navView.inflateMenu(R.menu.main_drawer);

    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
