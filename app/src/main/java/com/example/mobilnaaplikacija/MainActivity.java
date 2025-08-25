package com.example.mobilnaaplikacija;

import android.os.Bundle;

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

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private ActionBar actionBar;
    private AppBarConfiguration mAppBarConfiguration;

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

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Početni menu
        navigationView.inflateMenu(R.menu.logged_out_drawer);

        // Top-level destinacije (hamburger se prikazuje za ove)
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.mainFragment,   // dodaj mainFragment kao top-level
                R.id.nav_profile,
                R.id.profile_page,
                R.id.nav_register
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
            } else if (id == R.id.nav_logout) {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.logged_out_drawer);
                navController.navigate(R.id.homeFragment);
            }

            drawer.closeDrawers();
            return true;
        });
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
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void setMainDrawer() {
        NavigationView navView = findViewById(R.id.nav_view);
        navView.getMenu().clear();
        navView.inflateMenu(R.menu.main_drawer);

    }
}
