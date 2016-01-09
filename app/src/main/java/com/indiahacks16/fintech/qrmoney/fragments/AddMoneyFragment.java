package com.indiahacks16.fintech.qrmoney.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.indiahacks16.fintech.qrmoney.R;
import com.parse.ParseUser;

public class AddMoneyFragment extends Fragment {
    public static int accountBalance;
    TextView account_info;
    int account_balance;
    String account_balance_info;

    String input_added_money;
    int input_added_balance;

    FloatingActionButton add, ask;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_money, container, false);
        account_info = (TextView) view.findViewById(R.id.account_bal);
        final ParseUser currentUser = ParseUser.getCurrentUser();
        account_balance = currentUser.getInt("account_balance");
        account_balance_info = getResources().getString(R.string.account_info) + " Rs. " + account_balance;
        account_info.setText(account_balance_info);
        add = (FloatingActionButton) view.findViewById(R.id.add);
        ask = (FloatingActionButton) view.findViewById(R.id.ask);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getResources().getString(R.string.add_money_dialog_title));

                // Set up the input
                final EditText input = new EditText(getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        input_added_money = input.getText().toString();

                        if (isNumeric(input_added_money)) {

                            input_added_balance = Integer.parseInt(input_added_money);
                            account_balance = account_balance + input_added_balance;

                            final ParseUser c = ParseUser.getCurrentUser();
                            c.put("account_balance", account_balance);
                            accountBalance = account_balance;
                            account_balance_info = getResources().getString(R.string.account_info) + " Rs. " + account_balance;
                            account_info.setText(account_balance_info);

                        } else {
                            Toast.makeText(getContext(), R.string.add_money_error_toast, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
        return view;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}