package cpe.lesbarbus.cozynotes.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import cpe.lesbarbus.cozynotes.R;
import cpe.lesbarbus.cozynotes.adapter.NoteAdapter;
import cpe.lesbarbus.cozynotes.models.Note;
import cpe.lesbarbus.cozynotes.utils.CouchBaseNote;

public class NoteDetailActivity extends AppCompatActivity {

    private final String tag ="NoteDetailActivity";

    @Bind(R.id.note_detail_title)
    TextView _title;
    @Bind(R.id.note_detail_content)
    TextView _content;
    @Bind(R.id.note_detail_date)
    TextView _date;
    @Bind(R.id.note_detail_backbutton)
    ImageButton _backbutton;
    @Bind(R.id.note_detail_sharebutton)
    ImageButton _sharebutton;
    @Bind(R.id.note_detail_editbutton)
    ImageButton _editbutton;
    @Bind(R.id.note_detail_deletebutton)
    ImageButton _deletebutton;
    @Bind(R.id.note_detail_remindbutton)
    ImageButton _remindbutton;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        ButterKnife.bind(this);

        note = (Note) getIntent().getSerializableExtra("note");
        _title.setText(note.getTitle());
        _content.setText(Html.fromHtml(note.getContent()));
        _date.setText(note.getFormattedDateTime());

        _remindbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                Intent intentCal = new Intent(Intent.ACTION_EDIT);
                intentCal.setType("vnd.android.cursor.item/event");
                intentCal.putExtra("beginTime", cal.getTimeInMillis());
                intentCal.putExtra("allDay", true);
                intentCal.putExtra("rrule", "FREQ=YEARLY");
                intentCal.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
                intentCal.putExtra("title", note.getTitle());
                intentCal.putExtra("description",  Html.fromHtml(note.getContent()).toString());
                startActivity(intentCal);
            }
        });
        _editbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(NoteDetailActivity.this, EditNoteActivity.class);
                editIntent.putExtra("note", note);
                NoteDetailActivity.this.startActivity(editIntent);
                finish();
            }
        });
        _deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteDetailActivity.this);
                builder.setPositiveButton(R.string.alertdialog_delete_action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CouchBaseNote db = new CouchBaseNote();
                        db.deleteNote(note.get_id());
                        finish();

                    }
                });
                builder.setNegativeButton(R.string.alertdialog_cancel_action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //DO NOTHING
                    }
                });
                builder.setMessage(R.string.alertdialog_delete_note_message);
                builder.setTitle(R.string.alertdialog_delete_note_title);
                builder.create().show();
            }
        });
        _backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        _sharebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(note.getContent()).toString());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
