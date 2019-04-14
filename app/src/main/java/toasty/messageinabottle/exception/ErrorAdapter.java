package toasty.messageinabottle.exception;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import toasty.messageinabottle.R;
import toasty.messageinabottle.io.ExceptionContext;

public class ErrorAdapter extends RecyclerView.Adapter<ErrorAdapter.ErrorViewHolder> {

    private final List<ExceptionContext> exceptions;

    public ErrorAdapter() {
        exceptions = GlobalExceptionCache.get();
    }

    @NonNull
    @Override
    public ErrorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_error, parent, false);
        return new ErrorViewHolder(root);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ErrorViewHolder holder, int position) {
        ExceptionContext exceptionContext = exceptions.get(position);

        holder.errorTitle.setText(exceptionContext.getDate().toString() + " | " + exceptionContext.getTitle());

        StringWriter sw = new StringWriter();
        exceptionContext.getException().printStackTrace(new PrintWriter(sw));
        holder.errorContent.setText(sw.toString());
    }

    @Override
    public int getItemCount() {
        return exceptions.size();
    }

    public static class ErrorViewHolder extends RecyclerView.ViewHolder {

        public TextView errorTitle;
        public TextView errorContent;

        public ErrorViewHolder(@NonNull View itemView) {
            super(itemView);
            errorTitle = itemView.findViewById(R.id.error_title);
            errorContent = itemView.findViewById(R.id.error_content);

            Button clipboardButton = itemView.findViewById(R.id.clipboard_button);
            clipboardButton.setOnClickListener(v -> {
                Context ctx = itemView.getContext();
                ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("exception content", errorContent.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ctx, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
