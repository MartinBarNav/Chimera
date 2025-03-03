.class public Lcom/saterskog/mods/ExampleMod;
.super Ljava/lang/Object;
.source "ExampleMod.java"


# annotations
.annotation runtime Lcom/saterskog/cell_lab/ChimeraMod;
.end annotation


# direct methods
.method public constructor <init>()V
    .registers 1

    .line 6
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public unlockChallengeHook(I)Z
    .registers 3

    .line 9
    const/4 v0, 0x6

    if-le p1, v0, :cond_5

    const/4 p1, 0x1

    goto :goto_6

    :cond_5
    const/4 p1, 0x0

    :goto_6
    return p1
.end method
