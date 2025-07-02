package com.apvereda.digitalavatars.ui.additionalData;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.apvereda.db.Avatar;
import com.apvereda.digitalavatars.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdditionalDataActivity extends AppCompatActivity {

    LocalDate birthDate;
    String genre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_additional_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.filtrosLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextInputEditText inputFecha = findViewById(R.id.inputFecha);

        inputFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year1, month1, dayOfMonth) -> {
                        Calendar calendarAux = Calendar.getInstance();
                        calendarAux.set(year1, month1, dayOfMonth);
                        TextInputLayout inputLayout = findViewById(R.id.inputFechaLayout);
                        if (calendarAux.after(calendar)) {
                            inputLayout.setError("La fecha no puede ser posterior a la actual");
                        } else {
                            inputLayout.setError(null);
                            birthDate = LocalDate.of(year1, month1 + 1, dayOfMonth);
                            String fechaText = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
                            inputFecha.setText(fechaText);
                        }
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });

        MaterialButtonToggleGroup generoToggle = findViewById(R.id.generoToggle);
        generoToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {

            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                MaterialButton button = findViewById(checkedId);
                if (isChecked) {
                    button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    button.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
                    genre = button.getText().toString();
                } else {
                    button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
                    button.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
                    genre = null;
                }
            }
        });

        MaterialButton agregarDatosButton = findViewById(R.id.agregarDatosButton);
        agregarDatosButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(v.getContext())
                        .setTitle(R.string.alert_title)
                        .setMessage(R.string.alert_message)
                        .setPositiveButton(R.string.alert_accept, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Avatar avatar = Avatar.getAvatar();
                                Map<String, Object> additionalData = avatar.getAdditionalData();
                                additionalData.put("birthDate", birthDate.toString());
                                additionalData.put("genre", genre);
                                avatar.setAdditionalData(additionalData);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.alert_cancel, null)
                        .show();
            }
        });
    }
}