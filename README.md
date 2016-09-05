# GraphBuilder 0.1.2

GraphBuilder will be an open-source piece of software which allows users to visually construct [graphs](https://en.wikipedia.org/wiki/Graph_(abstract_data_type)) on a "editor" panel.

Currently, GraphBuilder is written in Java 7+ (developed in Eclipse).

Please note that GraphBuilder is still in its very early stages!

## Goals

Ultimately, users will be able to load and save their graphs, run algorithms on them, and export them as images.

## Code Overview

An overview of the packages and what they contain.

### actions

This package contains implementations of actions. Each action is a procedure the user can carry out in the editor. Some actions are reversible, which allows them to be undone. These actions are all implemented using the javax.swing.AbstractAction abstract class, since key bindings require a javax.swing.Action.

### components

This package contains the implementations of various graph components. These are essential for holding the data necessary to draw them on the editor panel. Currently, this contains implementations of nodes (circles) and edges (lines and arrows), the most basic components of a graph.

### context

This package contains a single context class which will contain data important for just about every part of the program, held in one central class. This data includes the set of nodes and edges of the graph currently being built, and the history of (reversible) actions the user has performed.

### io

This package contains utility classes for saving graphs to files and loading graphs from files.

### keybindings

This package currently contains code for keyboard shortcuts and a hard-coded mapping of keyboard shortcuts and corresponding actions. These key bindings are bound to the root pane of GraphBuilder's main window frame.

### math

This package contains classes for additional math beyond what Java's distribution comes with.

### preferences

This tentative package contains a list of user preferences. Ultimately, these will be moved to a configuration file.

### tool

This package contains a list of all the tools that the user can use in GraphBuilder.

### ui

This package contains classes related to the UI components necessary for user interaction. This includes the main window which appears when the program starts, and the editor panel that appears within it.

### ui.menus

This package contains menu UI components.

### ui.tooloptions

This package contains all the "option bars" for each of the tools.

### util

This package contains several utility classes whose methods will be used in several different places.
