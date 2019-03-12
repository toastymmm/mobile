package toasty.messageinabottle;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import toasty.messageinabottle.data.Message;

public class MessagePreviewAdapter extends RecyclerView.Adapter<MessagePreviewViewHolder> {

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
}
