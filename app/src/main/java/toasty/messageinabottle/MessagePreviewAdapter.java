package toasty.messageinabottle;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import toasty.messageinabottle.data.Message;

public class MessagePreviewAdapter extends RecyclerView.Adapter<MessagePreviewAdapter.MessagePreviewViewHolder> {

    private final List<Message> messages;

    public MessagePreviewAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessagePreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_message_preview, parent, false);
        return new MessagePreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagePreviewViewHolder messagePreviewViewHolder, int i) {
        messagePreviewViewHolder.update(messages.get(i));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MessagePreviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
            itemView.setOnClickListener(this);
        }

        public void update(Message message) {
            messagePreview.setText(message.getMsg());
            author.setText(message.getAuthor().getUsername());
            created.setText(message.getCreated().toString());
            saved.setText(message.getCreated().toString());
        }

        @Override
        public void onClick(View v) {
            Context ctx = v.getContext();
            int i = getAdapterPosition();
            Message message = messages.get(i);
            Intent intent = new Intent(ctx, MessageDetailActivity.class);
            intent.putExtra(MessageDetailActivity.MESSAGE_KEY, (Parcelable) message);
            ctx.startActivity(intent);
        }
    }
}
