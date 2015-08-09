package com.st.nicobot.bot.utils;

/**
 * Created by Logs on 09-08-15.
 * TODO Add standard Emojis
 */
public enum Emoji {

    BOWTIE("bowtie"),
    NECKBEARD("neckbeard"),
    METAL("metal"),
    FU("fu"),
    FEELSGOOD("feelsgood"),
    FINNADIE("finnadie"),
    GOBERSERK("goberserk"),
    GODMODE("godmode"),
    HURTREALBAD("hurtrealbad"),
    RAGE1("rage1"),
    RAGE2("rage2"),
    RAGE3("rage3"),
    RAGE4("rage4"),
    SUSPECT("suspect"),
    TROLLFACE("trollface"),
    OCTOCAT("octocat"),
    SQUIRREL("squirrel"),
    CRAB("crab"),
    PIGGY("piggy"),
    CUBIMAL_CHICK("cubimal_chick"),
    BERYL("beryl"),
    DUSTY_STICK("dusty_stick"),
    RUBE("rube"),
    TACO("taco"),
    SLACK("slack"),
    PRIDE("pride"),
    SHIPIT("shipit"),
    TROLL("troll"),
    WHITE_SQUARE("white_square"),
    BLACK_SQUARE("black_square"),
    SIMPLE_SMILE("simple_smile"),
    BOOBS("boobs"),
    BELGIUM("belgium"),
    NOTBAD("notbad"),
    YOLD("yold"),
    BOLD("bold"),
    FBLIKE("fblike"),
    FBDISLIKE("fbdislike");

    String emojiName;

    Emoji(String emojiName) {
        this.emojiName = emojiName;
    }

    public String getEmojiName() {
        return emojiName;
    }
}
