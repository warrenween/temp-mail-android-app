package com.tempmail.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tempmail.MainActivity;
import com.tempmail.R;
import com.tempmail.api.models.answers.Mails;
import com.tempmail.fragments.MailFragment;
import com.tempmail.fragments.MailsListFragment;
import com.tempmail.utils.Log;

import java.util.List;

/**
 * Created by Lotar on 05.07.2017.
 */

public class MailListAdapter extends RecyclerView.Adapter<MailListAdapter.ViewHolder> {
    private static final String TAG= MailListAdapter.class.getSimpleName();
    private List<Mails> mails;
    public Context context;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MailListAdapter(Context context, List<Mails> mails) {
        this.mails = mails;
        this.context = context;
        Log.d(TAG,"Size: "+ mails.size());
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MailListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mail, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Log.d(TAG, "position " + position);
        final  int mPosition= position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).navigateToFragment(MailFragment.newInstance(mails.get(mPosition)), true);
            }
        });
        Mails currentMails = mails.get(position);
        holder.tvSender.setText(currentMails.getMailFrom());
        holder.tvSubject.setText(currentMails.getMailSubject());
        if(position%2==0)
            holder.llItemMain.setBackgroundColor(context.getResources().getColor(R.color.colorLightBlue));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Log.d(TAG,"getItemCount size: "+ mails.size());
        return mails.size();
    }




    // Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
    class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView tvSender, tvSubject;
        View itemView;
        LinearLayout llItemMain;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView= itemView;
            tvSender = (TextView) itemView.findViewById(R.id.tvSender);
            tvSubject = (TextView) itemView.findViewById(R.id.tvSubject);
            llItemMain = (LinearLayout) itemView.findViewById(R.id.llItemMain);
        }
    }
}
