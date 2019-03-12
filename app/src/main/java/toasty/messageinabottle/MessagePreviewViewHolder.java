package toasty.messageinabottle;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import toasty.messageinabottle.data.Message;

public class MessagePreviewViewHolder extends RecyclerView.ViewHolder {

    private final TextView messagePreview;
    private final TextView author;
    private final TextView created;
    private final TextView saved;

    public MessagePreviewViewHolder(@NonNull View itemView) {
        super(itemView);
        messagePreview = itemView.findViewById(R.id.preview_message);
        author = itemView.findViewById(R.id.preview_author);
        created = itemView.findViewById(R.id.preview_created);
        saved = itemView.findViewById(R.id.preview_saved);
    }

    public void update(Message message) {
        messagePreview.setText(message.getMsg());
        author.setText(message.getAuthor().getUsername());
        created.setText(message.getCreated().toString());
        saved.setText(message.getCreated().toString());
    }
}
