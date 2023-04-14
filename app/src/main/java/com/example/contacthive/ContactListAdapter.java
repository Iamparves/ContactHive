package com.example.contacthive;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {
    Context context;
    ArrayList<ContactModel> arrContacts;
    private OnDataChangeListener listener;
    Animation contact_card_anim;

    ContactListAdapter(Context context, ArrayList<ContactModel> arrContacts) {
        this.context = context;
        this.arrContacts = arrContacts;
    }

    public interface OnDataChangeListener {
        void onDataChanged();
    }

    public void setOnDataChangeListener(OnDataChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String contactId, contactName, contactPhone, contactEmail, contactAddress, base64EncodedAvatar;

        contactId = Integer.toString(arrContacts.get(position).getId());
        contactName = arrContacts.get(position).getName();
        contactPhone = arrContacts.get(position).getPhone();
        contactEmail = arrContacts.get(position).getEmail();
        contactAddress = arrContacts.get(position).getAddress();
        base64EncodedAvatar = arrContacts.get(position).getAvatar();

        holder.tv_name.setText(contactName);
        holder.tv_phone.setText("Phone: " + contactPhone);
        holder.tv_email.setText("Email: " + contactEmail);
        holder.tv_address.setText("Address: " + contactAddress);

        if(contactEmail.equals("")) {
            holder.tv_email.setVisibility(View.GONE);
        }

        if(contactAddress.equals("")) {
            holder.tv_address.setVisibility(View.GONE);
        }

        byte[] avatarBytes = new byte[0];
        if(base64EncodedAvatar != null) {
            avatarBytes = Base64.decode(base64EncodedAvatar, Base64.DEFAULT);
            Bitmap bitmapAvatar = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);

            holder.iv_avatar.setImageBitmap(bitmapAvatar);
        }

        byte[] finalAvatarBytes = avatarBytes;
        holder.btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddContactActivity.class);

                intent.putExtra("isAddContact", false);
                intent.putExtra("contactId", contactId);
                intent.putExtra("contactName", contactName);
                intent.putExtra("contactPhone", contactPhone);
                intent.putExtra("contactEmail", contactEmail);
                intent.putExtra("contactAddress", contactAddress);
                intent.putExtra("contactAvatar", finalAvatarBytes);

                context.startActivity(intent);
            }
        });

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete contact");
                builder.setMessage("Are you sure you want to delete \"" + contactName + "\" from contacts?");
                builder.setIcon(R.drawable.ic_delete_forever);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContactDatabaseHelper contactDatabaseHelper = new ContactDatabaseHelper(context.getApplicationContext());
                        boolean success = contactDatabaseHelper.deleteOneContact(contactId);

                        if(success && listener != null) {
                            listener.onDataChanged();
                        }
                    }
                });

                builder.setNegativeButton("No", null);
                builder.create().show();
            }
        });

        holder.tv_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contactPhone));
                context.startActivity(intent);
            }
        });

        holder.tv_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + contactEmail));
                context.startActivity(Intent.createChooser(intent, "Send email"));
            }
        });

        holder.iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contactString = "Name: " + contactName + "\nPhone: " + contactPhone;

                if(!contactEmail.equals("")) {
                    contactString += "\nEmail: " + contactEmail;
                }

                if(!contactAddress.equals("")) {
                    contactString += "\nAddress: " + contactAddress;
                }

                contactString += "\n\n- ContactHive";

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, contactString);
                context.startActivity(Intent.createChooser(intent, "Share Contact"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_phone, tv_email, tv_address;
        ImageView iv_avatar, iv_share;
        Button btn_edit, btn_delete;
        LinearLayout ll_contactWrapper;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.tv_name);
            tv_phone = itemView.findViewById(R.id.tv_phone);
            tv_email = itemView.findViewById(R.id.tv_email);
            tv_address = itemView.findViewById(R.id.tv_address);
            iv_avatar = itemView.findViewById(R.id.iv_avatar);

            iv_share = itemView.findViewById(R.id.iv_share);
            btn_edit = itemView.findViewById(R.id.btn_edit);
            btn_delete = itemView.findViewById(R.id.btn_delete);

            ll_contactWrapper = itemView.findViewById(R.id.ll_contactWrapper);
            contact_card_anim = AnimationUtils.loadAnimation(context, R.anim.contact_card_anim);
            ll_contactWrapper.startAnimation(contact_card_anim);
        }
    }
}
