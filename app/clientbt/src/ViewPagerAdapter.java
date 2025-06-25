package balikbayan.box.client_bt;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private ArrayList<Fragment> array;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        array = new ArrayList<>();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return array.get(position);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public void add(Fragment fragment) {
        array.add(fragment);
    }

    public Fragment getItem(int position) {
        return array.get(position);
    }
}
