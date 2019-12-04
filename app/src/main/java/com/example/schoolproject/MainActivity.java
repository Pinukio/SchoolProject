package com.example.schoolproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import me.saket.inboxrecyclerview.InboxRecyclerView;
import me.saket.inboxrecyclerview.page.ExpandablePageLayout;
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks;


public class MainActivity extends AppCompatActivity {

    private InboxRecyclerView recyclerView;
    private Boolean isExpanded = false, isCreated = false, isEditable = true; // isEditable : 에딧텍스트를 수정할 수 있는 상태
    private int expandedPos, count = 0;
    private ArrayList<Item> list = new ArrayList<>();
    private FloatingActionButton fab;
    private RecyclerAdapter adapter;
    private EditText title, content, year, month, day;
    private String email;
    private InputMethodManager imm;
    private FirebaseFirestore db;
    private ExpandablePageLayout expandablePage;
    private androidx.appcompat.app.AlertDialog dialog;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        else {
            setContentView(R.layout.activity_main);

            email = auth.getCurrentUser().getEmail();
            db = FirebaseFirestore.getInstance();

            recyclerView = findViewById(R.id.Main_recycler);
            fab = findViewById(R.id.Main_fab);
            imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            builder.setView(inflater.inflate(R.layout.dialog_loading, null));
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.show();

            adapter = new RecyclerAdapter(list, this);
            title = findViewById(R.id.diaryTitle);
            year = findViewById(R.id.Main_year);
            month = findViewById(R.id.Main_month);
            day = findViewById(R.id.Main_day);
            content = findViewById(R.id.diaryContent);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            expandablePage = findViewById(R.id.Main_expandable);
            expandablePage.pushParentToolbarOnExpand(findViewById(R.id.Main_appbar));
            expandablePage.addStateChangeCallbacks(new SimplePageStateChangeCallbacks() {
                @Override
                public void onPageAboutToCollapse(long collapseAnimDuration) {
                    super.onPageAboutToCollapse(collapseAnimDuration);
                     if(list.get(expandedPos).title.isEmpty() && list.get(expandedPos).content.isEmpty()) {
                         list.remove(expandedPos);
                         adapter.notifyItemRemoved(expandedPos);
                         isExpanded = false;
                         fab.setImageDrawable(getResources().getDrawable(R.drawable.plus3));

                     }
                }
            });


            recyclerView.setExpandablePage(expandablePage);
            recyclerView.setAdapter(adapter);

            db.collection("users").document(email).collection("diaries")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        count = task.getResult().size();
                        if(count == 0)
                            dialog.dismiss();
                        int cnt = 0;
                        for(QueryDocumentSnapshot document : task.getResult()) {
                            getData(cnt);
                            cnt++;
                        }
                    }
                }
            });

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isExpanded) {

                        if(isEditable) { //체크 버튼
                            String titleText = title.getText().toString();
                            String contentText = content.getText().toString();
                            String yearText = year.getText().toString();
                            String monthText = month.getText().toString();
                            String dayText = day.getText().toString();
                            if(yearText.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "년도를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                            }

                            else if(monthText.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "월을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                            }

                            else if(dayText.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "날짜를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                            }

                            else if(titleText.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                            }

                            else if(contentText.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "내용을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                list.get(expandedPos).title = titleText;
                                list.get(expandedPos).year = yearText;
                                list.get(expandedPos).month = monthText;
                                list.get(expandedPos).day = dayText;
                                list.get(expandedPos).content = contentText;

                                adapter.notifyDataSetChanged();
                                fab.setImageDrawable(getResources().getDrawable(R.drawable.pencil_w));
                                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                                deleteFocus();
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("title", titleText);
                                map.put("year", yearText);
                                map.put("month", monthText);
                                map.put("day", dayText);
                                map.put("content", contentText);

                                db.collection("users").document(email).collection("diaries").document(String.valueOf(expandedPos)).set(map);
                                Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
                                isEditable = false;
                                isCreated = false;
                            }
                        }

                        else { //수정 버튼
                            addFocus();
                            isEditable = true;
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.check));
                            title.requestFocus();
                            imm.showSoftInput(title, 0);
                        }

                    }
                    else {
                        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                        String[] arr = date.split("-");
                        list.add(new Item("", arr[0], arr[1], arr[2], ""));
                        create((long)(list.size() - 1));
                        title.requestFocus();
                        imm.showSoftInput(title, 0);
                    }

                }
            });

            TextView tv = findViewById(R.id.Main_title);
            final AlertDialog.Builder builder_ = new AlertDialog.Builder(this);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder_.setMessage("로그아웃하시겠습니까?");
                    builder_.setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            auth.signOut();
                            Toast.makeText(getApplicationContext(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            finish();
                        }
                    });
                    builder_.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder_.show();
                }
            });
        }
    }

    public void expand(long id) {
        recyclerView.expandItem(id);
        String titleText = list.get((int)id).title;
        String yearText = list.get((int)id).year;
        String monthText = list.get((int)id).month;
        String dayText = list.get((int)id).day;
        String contentText = list.get((int)id).content;
        title.setText(titleText);
        year.setText(yearText);
        month.setText(monthText);
        day.setText(dayText);
        content.setText(contentText);

        isExpanded = true;
        isEditable = false;
        deleteFocus();
        expandedPos = (int)id;

        fab.setImageDrawable(getResources().getDrawable(R.drawable.pencil_w));
    }

    public void create(long id) {

        recyclerView.expandItem(id);
        String titleText = list.get((int)id).title;
        String yearText = list.get((int)id).year;
        String monthText = list.get((int)id).month;
        String dayText = list.get((int)id).day;
        String contentText = list.get((int)id).content;
        title.setText(titleText);
        year.setText(yearText);
        month.setText(monthText);
        day.setText(dayText);
        content.setText(contentText);

        isExpanded = true;
        expandedPos = (int)id;
        isEditable = true;

        adapter.notifyItemInserted(list.size() - 1);
        fab.setImageDrawable(getResources().getDrawable(R.drawable.check));
        isCreated = true;

        addFocus();
    }

    @Override
    public void onBackPressed() {
        if(isExpanded) {

            final String titleText = title.getText().toString();
            final String cardTitleText = list.get(expandedPos).title;
            final String yearText = year.getText().toString();
            final String cardYearText = list.get(expandedPos).year;
            final String monthText = month.getText().toString();
            final String cardMonthText = list.get(expandedPos).month;
            final String dayText = day.getText().toString();
            final String cardDayText = list.get(expandedPos).day;
            final String contentText = content.getText().toString();
            final String cardContentText = list.get(expandedPos).content;

            final boolean isUnsaved = !titleText.equals(cardTitleText) || !contentText.equals(cardContentText) || !yearText.equals(cardYearText) || !monthText.equals(cardMonthText) || !dayText.equals(cardDayText);
            if(isUnsaved || isCreated) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("저장하지 않고 나가시겠습니까?");
                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(isCreated) {
                            dialog.dismiss();
                            title.setText("");
                            content.setText("");
                            recyclerView.collapse();
                            isExpanded = false;
                            isCreated = false;
                        }

                        else if(isUnsaved) {

                            recyclerView.collapse();
                            isExpanded = false;
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.plus3));
                        }

                    }
                });
                builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }

            else if(isEditable) {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.pencil_w));
                deleteFocus();
                isEditable = false;
            }

            else {
                recyclerView.collapse();
                isExpanded = false;
                fab.setImageDrawable(getResources().getDrawable(R.drawable.plus3));
            }
        }
        else
            super.onBackPressed();
    }

    private void getData(final int pos) {
        db.collection("users").document(email).collection("diaries").document(String.valueOf(pos))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                String titleText = document.get("title").toString();
                String contentText = document.get("content").toString();
                String yearText = document.get("year").toString();
                String monthText = document.get("month").toString();
                String dayText = document.get("day").toString();
                list.add(new Item(titleText, yearText, monthText , dayText, contentText));
                adapter.notifyItemInserted(pos);
                if(pos == count - 1)
                    dialog.dismiss();
            }
        });
    }

    private void deleteFocus() {
        title.setFocusable(false);
        year.setFocusable(false);
        month.setFocusable(false);
        day.setFocusable(false);
        content.setFocusable(false);
    }

    private void addFocus() {
        title.setFocusableInTouchMode(true);
        title.setFocusable(true);
        year.setFocusableInTouchMode(true);
        year.setFocusable(true);
        month.setFocusableInTouchMode(true);
        month.setFocusable(true);
        day.setFocusableInTouchMode(true);
        day.setFocusable(true);
        content.setFocusableInTouchMode(true);
        content.setFocusable(true);
    }
}
