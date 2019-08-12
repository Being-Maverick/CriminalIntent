package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

public class CrimeListFragment extends Fragment {

    //  private Crime modifiedCrime;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private boolean mSubtitleVisible;
    private CallBacks mCallBacks;
    private ItemTouchHelper mItemTouchHelper;

    public interface CallBacks{
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallBacks = (CallBacks)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBacks = null;
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mTitle;
        private TextView mDate;
        private ImageView mImageView;
        private Crime mCrime;


        public CrimeHolder(LayoutInflater inflater, ViewGroup group/*,int layout*/){
            super(inflater.inflate(/*layout*/R.layout.list_crime_item,group,false));

            mTitle = (TextView) itemView.findViewById(R.id.crime_title);
            mDate = (TextView) itemView.findViewById(R.id.crime_date);
            mImageView = (ImageView) itemView.findViewById(R.id.crime_solved);
            itemView.setOnClickListener(this);
        }

        public void bind(Crime crime){
            mCrime = crime;
            mTitle.setText(mCrime.getTitle());
            Date date = crime.getDate();
            //String format = "%b d, Y";
            mDate.setText(date.toString());
            mImageView.setVisibility(crime.isSolved()? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            // Intent intent = CrimePagerActivity.newIntent(getActivity(),mCrime.getId());
            // startActivityForResult(intent, Activity.RESULT_OK);
           // modifiedCrime = mCrime;
            mCallBacks.onCrimeSelected(mCrime);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{
        private List<Crime> mCrimeList;

        public CrimeAdapter(List<Crime> crimes){
            mCrimeList = crimes;
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimeList = crimes;
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            Crime crime = mCrimeList.get(position);
            holder.bind(crime);
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            //  int layout = getItemViewType(i);
            return new CrimeHolder(inflater,viewGroup/*,i*/);
        }

        @Override
        public int getItemCount() {
            return mCrimeList.size();
        }

        //@Override
        //public int getItemViewType(int position) {
        //    boolean mRequiresPolice = mCrimeList.get(position).isRequiresPolice();
        //    if(mRequiresPolice){
        //        return R.layout.list_crime_item2;
        //    }
        //    return R.layout.list_crime_item;
        //}
    }

    private RecyclerView mRecyclerView;
    private CrimeAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list,container,false);

        if(savedInstanceState != null){
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE,false);
        }
        mRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        initItemTouchHelper();
        updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        updateSubtitle();
    }

    public void updateUI(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if(mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mRecyclerView.setAdapter(mAdapter);
        }
        else {
            //  mAdapter.notifyItemChanged(crimes.indexOf(modifiedCrime));
            mAdapter.setCrimes(crimes);
          mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list,menu);

        MenuItem menuItem = menu.findItem(R.id.show_subtitle);
        if(mSubtitleVisible){
            menuItem.setTitle(R.string.hide_subtitle);
        }else{
            menuItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                // Intent intent = CrimePagerActivity.newIntent(getActivity(),crime.getId());
                // startActivity(intent);
                updateUI();
                mCallBacks.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural,crimeCount,crimeCount);

        if(!mSubtitleVisible){
            subtitle = null;
        }
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE,mSubtitleVisible);
    }

    public void initItemTouchHelper(){
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if(i == ItemTouchHelper.RIGHT){
                    CrimeLab crimeLab = CrimeLab.get(getActivity());
                    Crime crime = mAdapter.mCrimeList.get(viewHolder.getAdapterPosition());
                    crimeLab.deleteCrime(crime);
                }
            }
        });
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }
}
