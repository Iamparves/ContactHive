package com.example.contacthive;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AddContactActivity extends AppCompatActivity {
    private static final int IMAGE_REQUEST_CODE = 1000;
    private String base64EncodedAvatar;
    EditText et_name, et_phone, et_email, et_address;
    TextView errTxt_name, errTxt_phone;
    Button btn_add;
    FloatingActionButton btn_uploadImage;
    ImageView iv_avatar;
    String contactId, contactName, contactPhone, contactEmail, contactAddress;
    Bitmap bitmapAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        et_name = findViewById(R.id.et_name);
        et_phone = findViewById(R.id.et_phone);
        et_email = findViewById(R.id.et_email);
        et_address = findViewById(R.id.et_address);
        btn_add = findViewById(R.id.btn_add);
        btn_uploadImage = findViewById(R.id.btn_uploadImage);
        iv_avatar = findViewById(R.id.iv_avatar);
        errTxt_name = findViewById(R.id.errTxt_name);
        errTxt_phone = findViewById(R.id.errTxt_phone);

        Intent intent = getIntent();
        boolean isAddContact = intent.getBooleanExtra("isAddContact", true);

        if(!isAddContact) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("Edit an existing contact");

            contactId = intent.getStringExtra("contactId");
            contactName = intent.getStringExtra("contactName");
            contactPhone = intent.getStringExtra("contactPhone");
            contactEmail = intent.getStringExtra("contactEmail");
            contactAddress = intent.getStringExtra("contactAddress");
            byte[] contactAvatar = intent.getByteArrayExtra("contactAvatar");
            bitmapAvatar = BitmapFactory.decodeByteArray(contactAvatar, 0, contactAvatar.length);

            et_name.setText(contactName);
            et_phone.setText(contactPhone);
            et_email.setText(contactEmail);
            et_address.setText(contactAddress);
            btn_add.setText("Update contact");

            // try to remove the implementation
            if(bitmapAvatar == null) {
                Drawable drawableAvatar = getResources().getDrawable(R.drawable.avatar);
                iv_avatar.setImageDrawable(drawableAvatar);
            } else {
                iv_avatar.setImageBitmap(bitmapAvatar);
            }
        }

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactModel contactModel = null;
                String contactName, contactPhone, contactEmail, contactAddress;
                contactName = et_name.getText().toString();
                contactPhone = et_phone.getText().toString();
                contactEmail = et_email.getText().toString();
                contactAddress = et_address.getText().toString();

                if(contactName.equals("") || contactPhone.equals("")) {
                    if(contactName.equals("")) {
                        errTxt_name.setVisibility(View.VISIBLE);
                    } else {
                        errTxt_name.setVisibility(View.GONE);
                    }

                    if(contactPhone.equals("")) {
                        errTxt_phone.setVisibility(View.VISIBLE);
                    } else {
                        errTxt_phone.setVisibility(View.GONE);
                    }

                    return;
                } else {
                    errTxt_name.setVisibility(View.GONE);
                    errTxt_phone.setVisibility(View.GONE);
                }

                try {
                    base64EncodedAvatar = base64EncodedAvatar != null ? base64EncodedAvatar : bitmapToBase64(bitmapAvatar);

                    contactModel = new ContactModel(-1, contactName, contactPhone, contactEmail, contactAddress, base64EncodedAvatar);
                    ContactDatabaseHelper contactDatabaseHelper = new ContactDatabaseHelper(AddContactActivity.this);

                    boolean success;
                    if(isAddContact) {
                        success = contactDatabaseHelper.addContact(contactModel);
                    } else {
                        success = contactDatabaseHelper.updateContact(contactModel, contactId);
                    }

                    if(success) {
                        Toast.makeText(AddContactActivity.this, "Contact " + (isAddContact ? "added" : "updated") + " successfully", Toast.LENGTH_SHORT).show();
                        NavUtils.navigateUpFromSameTask(AddContactActivity.this);
                    } else {
                        Toast.makeText(AddContactActivity.this, "Couldn't " + (isAddContact ? "add" : "update") + " the contact 1", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    Toast.makeText(AddContactActivity.this, "Couldn't " + (isAddContact ? "add" : "update") + " the contact 2", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iAvatar = new Intent(Intent.ACTION_PICK);
                iAvatar.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iAvatar, IMAGE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == IMAGE_REQUEST_CODE) {
                Uri avatarUri = data.getData();
                byte[] avatarBytes = getBytesFromUri(getApplicationContext(), avatarUri);
                base64EncodedAvatar = getBase64ScaledImage(avatarBytes, 1000, 1000);
                iv_avatar.setImageURI(avatarUri);
            }
        }
    }

    static byte[] getBytesFromUri(Context context, Uri uri) {
        InputStream inputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buff = new byte[bufferSize];
            int len = 0;
            while ((len = inputStream.read(buff)) != -1) {
                byteBuff.write(buff, 0, len);
            }
            return byteBuff.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getBase64ScaledImage(byte[] byteArray, int desiredWidth, int desiredHeight) {
        // Convert byte array to bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        // Check orientation and rotate the bitmap if necessary
        try {
            ExifInterface exif;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                exif = new ExifInterface(new ByteArrayInputStream(byteArray));
            } else {
                File tempFile = File.createTempFile("temp", null);
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(byteArray);
                exif = new ExifInterface(tempFile.getAbsolutePath());
            }

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Determine original dimensions
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        // Determine scale factor
        float scaleFactorX = (float) desiredWidth / originalWidth;
        float scaleFactorY = (float) desiredHeight / originalHeight;
        float scaleFactor = Math.min(scaleFactorX, scaleFactorY);

        // Create a new bitmap object with the desired dimensions
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (originalWidth * scaleFactor),
                (int) (originalHeight * scaleFactor),
                false);

        // convert bitmap to byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boolean imgTooBig = originalWidth > 4500 || originalHeight > 4500;
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, imgTooBig ? 80 : 90, byteArrayOutputStream);
        byte[] imageByteArray = byteArrayOutputStream.toByteArray();

        // encode byte array to base64 string
        String base64String = Base64.encodeToString(imageByteArray, Base64.DEFAULT);

        return base64String;
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        if(bitmap == null) return null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}