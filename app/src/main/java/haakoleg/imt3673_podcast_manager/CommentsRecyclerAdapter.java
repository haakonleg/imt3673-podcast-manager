package haakoleg.imt3673_podcast_manager;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import haakoleg.imt3673_podcast_manager.models.Comment;

/**
 * Adapter for the RecyclerView for displaying user comments
 */

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.CommentHolder> {
    private final List<Comment> comments;

    public CommentsRecyclerAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        notifyItemInserted(comments.size());
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_comment, parent, false);
        return new CommentHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.usernameTxt.setText(comment.getUsername());
        holder.rating.setRating((float) comment.getRating());
        holder.commentTxt.setText(comment.getComment());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    /**
     * ViewHolder for a user comment
     */
    class CommentHolder extends RecyclerView.ViewHolder {
        final TextView usernameTxt;
        final RatingBar rating;
        final TextView commentTxt;

        CommentHolder(View itemView) {
            super(itemView);
            usernameTxt = itemView.findViewById(R.id.username_txt);
            rating = itemView.findViewById(R.id.comment_rating);
            commentTxt = itemView.findViewById(R.id.comment_txt);
        }
    }
}
