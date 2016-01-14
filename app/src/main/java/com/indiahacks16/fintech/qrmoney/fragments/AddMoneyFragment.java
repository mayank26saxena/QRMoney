package com.indiahacks16.fintech.qrmoney.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.indiahacks16.fintech.qrmoney.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class AddMoneyFragment extends Fragment {
    public static int accountBalance;
    TextView account_info;
    int account_balance;
    String account_balance_info;
    String input_added_money;
    int input_added_balance;
    FloatingActionButton add, ask;
    String name ;
    TextView welcome_tv;
    Button testPush;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_money, container, false);
        account_info = (TextView) view.findViewById(R.id.account_bal);
        add = (FloatingActionButton) view.findViewById(R.id.add);
        ask = (FloatingActionButton) view.findViewById(R.id.ask);
        welcome_tv = (TextView) view.findViewById(R.id.welcome_text);
        testPush = (Button) view.findViewById(R.id.testPush1);
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            account_balance = currentUser.getInt("account_balance");
            account_balance_info = getResources().getString(R.string.account_info) + " Rs. " + account_balance;
            account_info.setText(account_balance_info);
            name = currentUser.get("Full_Name").toString();
            welcome_tv.setText(getResources().getString(R.string.welcome_msg) + " " + name + ".");
        }
        testPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(testPush, "Button Pressed", Snackbar.LENGTH_LONG).show();
                Log.i(this.getClass().getSimpleName(), "Inside Listener");
                final ParseQuery<ParseInstallation> query2 = ParseInstallation.getQuery();
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username", "9650232753");
                query.getFirstInBackground(new GetCallback<ParseUser>() {
                    public void done(ParseUser object, ParseException e) {
                        if (e == null) {
                            Log.v(this.getClass().getSimpleName(), "## " + object.getString("Full_Name"));
                            Log.v(this.getClass().getSimpleName(), "$$ " + object.getObjectId());
                            query2.whereEqualTo("objectId", object.get("objectId"));
                            ParsePush push = new ParsePush();
                            push.setQuery(query2);
                            Log.v(this.getClass().getSimpleName(), "Push Query Set");
                            JSONObject data = new JSONObject();
                            try {
                                data.put("message", "Hello World");
                                data.put("type", "withdrawal");
                                data.put("amount", 100.0);
                            } catch (JSONException e1) {
                                Log.v(this.getClass().getSimpleName(), "Push Data error");
                                e1.printStackTrace();
                            }
                            push.setData(data);
                            Log.v(this.getClass().getSimpleName(), "Push Data Set");
                            push.sendInBackground(new SendCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null)
                                        Log.v(this.getClass().getSimpleName(), "Push successfully sent");
                                    else
                                        Log.v(this.getClass().getSimpleName(), "Error : " + e.getMessage());
                                }
                            });
                        } else {
                            Log.v(this.getClass().getSimpleName(), "Some error");
                        }
                    }
                });
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getResources().getString(R.string.add_money_dialog_title));
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        input_added_money = input.getText().toString();
                        if (isNumeric(input_added_money)) {
                            input_added_balance = Integer.parseInt(input_added_money);
                            account_balance = account_balance + input_added_balance;
                            final ParseUser c = ParseUser.getCurrentUser();
                            c.put("account_balance", account_balance);
                            c.saveInBackground();
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