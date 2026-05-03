package com.aditya.blackjack.engine;

import com.aditya.blackjack.domain.table.Table;
import com.aditya.blackjack.domain.table.TableConfig;

public class Game {
    private Table table;

    public Game(TableConfig config) {
    }

    public void start() {
    }      // main loop: new Round until player quits

    public void setup() {
    }      // assign player to seat(s), set balances

    public Table getTable() {
        return table;
    }
}
