package org.pl.android.drively.data.model.chip;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.tylersuehr.chips.Chip;

import lombok.Data;
import lombok.NonNull;

@Data
public class CategoryChip extends Chip {

    @NonNull private String categoryName;

    @Override
    public Object getId() {
        return null;
    }

    @android.support.annotation.NonNull
    @Override
    public String getTitle() {
        return categoryName;
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return categoryName;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

}
