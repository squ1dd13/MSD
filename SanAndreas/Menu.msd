class Menu {
    [0x8D6]
    void setColumnOrientation(Int a, Int b);

    [0x8D7]
    void getHighlighted(GlobalIntFloat highlightedItem);

    [0x8D8]
    void getLastChosen(GlobalIntFloat chosenItem);

    [0x8DA]
    void delete();

    [0x8DB]
    void setColumnContents(Int column,
                           Str title,
                           Str item1,
                           Str item2,
                           Str item3,
                           Str item4,
                           Str item5,
                           Str item6,
                           Str item7,
                           Str item8,
                           Str item9,
                           Str item10,
                           Str item11,
                           Str item12);

    [0x9DB]
    void setColumnWidth(Int column, Int width);

    [0x8EE]
    void setItemWithNumber(Int column, Int row, Str stringValue, GlobalIntFloat numberValue);
}