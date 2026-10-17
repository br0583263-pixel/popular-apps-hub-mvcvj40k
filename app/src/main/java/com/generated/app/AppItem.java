package com.generated.app;

import android.graphics.drawable.Drawable;

public class AppItem {
    public String id;
    public String name;
    public String packageName;
    public String category;
    public String description;
    public String webFallbackUrl;
    public boolean isSystemApp;
    public boolean isInstalled;
    public boolean isFavorite;
    public Drawable resolvedIcon;

    public AppItem(String id, String name, String packageName, String category, String description, String webFallbackUrl) {
        this.id = id;
        this.name = name;
        this.packageName = packageName;
        this.category = category;
        this.description = description;
        this.webFallbackUrl = webFallbackUrl;
        this.isSystemApp = false;
        this.isInstalled = false;
        this.isFavorite = false;
        this.resolvedIcon = null;
    }
}
