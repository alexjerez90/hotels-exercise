package example.com.hotels.ui.list;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import example.com.hotels.CustomApplication;
import example.com.hotels.R;
import example.com.hotels.data.HotelManager;
import example.com.hotels.data.model.Hotel;
import example.com.hotels.ui.details.DetailsFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class ListFragment extends Fragment implements ListContract.View {

    private final static String LOG_TAG = "ListFragment";

    private Unbinder unbinder;
    private ListContract.Presenter presenter;

    @Inject
    HotelManager dataManager;
    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.listContainer)
    View container;

    private ListAdapter listAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CustomApplication.getAppComponent().inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new ListPresenter(this, dataManager, AndroidSchedulers.mainThread(), Schedulers.io());
    }

    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setUpActionBar();
        setUpHotelList();
        presenter.getHotels();

        return rootView;
    }

    private void setUpActionBar() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    private void setUpHotelList() {
        list.setHasFixedSize(false);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(mLayoutManager);
        listAdapter = new ListAdapter(onHotelClick);
        list.setAdapter(listAdapter);
    }

    private OnHotelClick onHotelClick = (Hotel hotel) -> {
        final String tag = "details";
        DetailsFragment details = DetailsFragment.createInstance(hotel);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainContainer, details, tag)
                .addToBackStack(tag)
                .commit();
    };

    interface OnHotelClick {
        void onClick(Hotel hotel);
    }

    @Override
    public void onSuccess(List<Hotel> list) {
        progressBar.setVisibility(View.GONE);
        listAdapter.setData(list);
    }

    @Override
    public void onError(Throwable throwable) {
        progressBar.setVisibility(View.GONE);
        Snackbar.make(container, "Connection error. Try again later", Snackbar.LENGTH_LONG)
                .show();
        Log.e(LOG_TAG, "Hotels error", throwable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
