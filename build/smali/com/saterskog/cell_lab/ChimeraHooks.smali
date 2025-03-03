.class public Lcom/saterskog/cell_lab/ChimeraHooks;
.super Ljava/lang/Object;
.source "ChimeraHooks.java"


# static fields
.field private static mods:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List<",
            "Ljava/lang/Object;",
            ">;"
        }
    .end annotation
.end field


# direct methods
.method static constructor <clinit>()V
    .registers 1

    .line 8
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    sput-object v0, Lcom/saterskog/cell_lab/ChimeraHooks;->mods:Ljava/util/List;

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .line 7
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static initMods([Ljava/lang/String;)V
    .registers 6

    .line 11
    array-length v0, p0

    const/4 v1, 0x0

    const/4 v2, 0x0

    :goto_3
    if-ge v2, v0, :cond_2c

    aget-object v3, p0, v2

    .line 13
    :try_start_7
    invoke-static {v3}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v3

    .line 14
    const-class v4, Lcom/saterskog/cell_lab/ChimeraMod;

    invoke-virtual {v3, v4}, Ljava/lang/Class;->isAnnotationPresent(Ljava/lang/Class;)Z

    move-result v4

    if-eqz v4, :cond_24

    .line 15
    new-array v4, v1, [Ljava/lang/Class;

    invoke-virtual {v3, v4}, Ljava/lang/Class;->getDeclaredConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;

    move-result-object v3

    new-array v4, v1, [Ljava/lang/Object;

    invoke-virtual {v3, v4}, Ljava/lang/reflect/Constructor;->newInstance([Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v3

    .line 16
    sget-object v4, Lcom/saterskog/cell_lab/ChimeraHooks;->mods:Ljava/util/List;

    invoke-interface {v4, v3}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    :try_end_24
    .catch Ljava/lang/Exception; {:try_start_7 .. :try_end_24} :catch_25

    .line 20
    :cond_24
    goto :goto_29

    .line 18
    :catch_25
    move-exception v3

    .line 19
    invoke-virtual {v3}, Ljava/lang/Exception;->printStackTrace()V

    .line 11
    :goto_29
    add-int/lit8 v2, v2, 0x1

    goto :goto_3

    .line 22
    :cond_2c
    return-void
.end method

.method public static varargs invokeMethod(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Class;[Ljava/lang/Object;)V
    .registers 5
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/Object;",
            "Ljava/lang/String;",
            "[",
            "Ljava/lang/Class<",
            "*>;[",
            "Ljava/lang/Object;",
            ")V"
        }
    .end annotation

    .line 67
    :try_start_0
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v0

    invoke-virtual {v0, p1, p2}, Ljava/lang/Class;->getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;

    move-result-object p1

    .line 68
    invoke-virtual {p1, p0, p3}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    :try_end_b
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_b} :catch_c

    .line 71
    goto :goto_10

    .line 69
    :catch_c
    move-exception p0

    .line 70
    invoke-virtual {p0}, Ljava/lang/Exception;->printStackTrace()V

    .line 72
    :goto_10
    return-void
.end method

.method public static invokeMethodNoParams(Ljava/lang/Object;Ljava/lang/String;)V
    .registers 5

    .line 58
    :try_start_0
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v0

    const/4 v1, 0x0

    new-array v2, v1, [Ljava/lang/Class;

    invoke-virtual {v0, p1, v2}, Ljava/lang/Class;->getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;

    move-result-object p1

    .line 59
    new-array v0, v1, [Ljava/lang/Object;

    invoke-virtual {p1, p0, v0}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    :try_end_10
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_10} :catch_11

    .line 62
    goto :goto_15

    .line 60
    :catch_11
    move-exception p0

    .line 61
    invoke-virtual {p0}, Ljava/lang/Exception;->printStackTrace()V

    .line 63
    :goto_15
    return-void
.end method

.method public static unlockChallengeHook(I)Z
    .registers 9

    .line 30
    sget-object v0, Lcom/saterskog/cell_lab/ChimeraHooks;->mods:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->isEmpty()Z

    move-result v0

    const/4 v1, 0x0

    if-eqz v0, :cond_a

    return v1

    .line 32
    :cond_a
    sget-object v0, Lcom/saterskog/cell_lab/ChimeraHooks;->mods:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :goto_10
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_5d

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .line 34
    :try_start_1a
    invoke-virtual {v2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v3

    const-string v4, "unlockChallengeHook"

    const/4 v5, 0x1

    new-array v6, v5, [Ljava/lang/Class;

    sget-object v7, Ljava/lang/Integer;->TYPE:Ljava/lang/Class;

    aput-object v7, v6, v1

    invoke-virtual {v3, v4, v6}, Ljava/lang/Class;->getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;

    move-result-object v3
    :try_end_2b
    .catch Ljava/lang/NoSuchMethodException; {:try_start_1a .. :try_end_2b} :catch_5a
    .catch Ljava/lang/Exception; {:try_start_1a .. :try_end_2b} :catch_55

    .line 36
    :try_start_2b
    new-array v4, v5, [Ljava/lang/Object;

    invoke-static {p0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v5

    aput-object v5, v4, v1

    invoke-virtual {v3, v2, v4}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Ljava/lang/Boolean;

    invoke-virtual {v2}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v2
    :try_end_3d
    .catch Ljava/lang/IllegalArgumentException; {:try_start_2b .. :try_end_3d} :catch_41
    .catch Ljava/lang/NoSuchMethodException; {:try_start_2b .. :try_end_3d} :catch_5a
    .catch Ljava/lang/Exception; {:try_start_2b .. :try_end_3d} :catch_55

    .line 37
    if-eqz v2, :cond_40

    return v2

    .line 42
    :cond_40
    goto :goto_5b

    .line 38
    :catch_41
    move-exception v2

    .line 40
    :try_start_42
    invoke-static {p0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v2

    new-array v4, v1, [Ljava/lang/Object;

    invoke-virtual {v3, v2, v4}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Ljava/lang/Boolean;

    invoke-virtual {v2}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v2
    :try_end_52
    .catch Ljava/lang/NoSuchMethodException; {:try_start_42 .. :try_end_52} :catch_5a
    .catch Ljava/lang/Exception; {:try_start_42 .. :try_end_52} :catch_55

    .line 41
    if-eqz v2, :cond_5b

    return v2

    .line 45
    :catch_55
    move-exception v2

    .line 46
    invoke-virtual {v2}, Ljava/lang/Exception;->printStackTrace()V

    goto :goto_5c

    .line 43
    :catch_5a
    move-exception v2

    .line 47
    :cond_5b
    :goto_5b
    nop

    .line 48
    :goto_5c
    goto :goto_10

    .line 51
    :cond_5d
    return v1
.end method
