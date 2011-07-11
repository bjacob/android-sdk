/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ide.common.api;

import java.util.List;


/**
 * An {@link IViewRule} describes the rules that apply to a given Layout or View object
 * in the Graphical Layout Editor.
 * <p/>
 * Rules are implemented by builtin layout helpers, or 3rd party layout rule implementations
 * provided with or for a given 3rd party widget.
 * <p/>
 * A 3rd party layout rule should use the same fully qualified class name as the layout it
 * represents, plus "Rule" as a suffix. For example, the layout rule for the
 * LinearLayout class is LinearLayoutRule, in the same package.
 * <p/>
 * Rule instances are stateless. They are created once per View class to handle and are shared
 * across platforms or editor instances. As such, rules methods should never cache editor-specific
 * arguments that they might receive.
 * <p/>
 * <b>NOTE: This is not a public or final API; if you rely on this be prepared
 * to adjust your code for the next tools release.</b>
 * </p>
 */
public interface IViewRule {

    /**
     * This method is called by the rule engine when the script is first loaded.
     * It gives the rule a chance to initialize itself.
     *
     * @param fqcn The fully qualified class name of the Layout or View that will be managed by
     *   this rule. This can be cached as it will never change for the lifetime of this rule
     *   instance. This may or may not match the script's filename as it may be the fqcn of a
     *   class derived from the one this rule can handle.
     * @param engine The engine that is managing the rules. A rule can store a reference to
     *   the engine during initialization and then use it later to invoke some of the
     *   {@link IClientRulesEngine} methods for example to request user input.
     * @return True if this rule can handle the given FQCN. False if the rule can't handle the
     *   given FQCN, in which case the rule engine will find another rule matching a parent class.
     */
    boolean onInitialize(String fqcn, IClientRulesEngine engine);

    /**
     * This method is called by the rules engine just before the script is unloaded.
     */
    void onDispose();

    /**
     * Returns the class name to display when an element is selected in the GLE.
     * <p/>
     * If null is returned, the GLE will automatically shorten the class name using its
     * own heuristic, which is to keep the first 2 package components and the class name.
     * The class name is the <code>fqcn</code> argument that was given
     * to {@link #onInitialize(String)}.
     *
     * @return Null for the default behavior or a shortened string.
     */
    String getDisplayName();

    /**
     * Invoked by the Rules Engine to retrieve a set of actions to customize
     * the context menu displayed for this view. The result is not cached and the
     * method is invoked every time the context menu is about to be shown.
     * <p/>
     * Most rules should consider returning <code>super.getContextMenu(node)</code>
     * and appending their own custom menu actions, if any.
     * <p/>
     * Menu actions are either toggles or fixed lists with one currently-selected
     * item. It is expected that the rule will need to recreate the actions with
     * different selections when a menu is going to shown, which is why the result
     * is not cached. However rules are encouraged to cache some or all of the result
     * to speed up following calls if it makes sense.
     *
     * @return Null for no context menu, or a new {@link MenuAction} describing one
     *   or more actions to display in the context menu.
     */
    List<MenuAction> getContextMenu(INode node);

    /**
     * Invoked by the Rules Engine to ask the parent layout for the set of layout actions
     * to display in the layout bar. The layout rule should add these into the provided
     * list. The order the items are added in does not matter; the
     * {@link MenuAction#getSortPriority()} values will be used to sort the actions prior
     * to display, which makes it easier for parent rules and deriving rules to interleave
     * their respective actions.
     *
     * @param actions the list of actions to add newly registered actions into
     * @param parentNode the parent of the selection, or the selection itself if the root
     * @param targets the targeted/selected nodes, if any
     */
    void addLayoutActions(List<MenuAction> actions,
            INode parentNode, List<? extends INode> targets);

    // ==== Selection ====

    /**
     * Returns a list of strings that will be displayed when a single child is being
     * selected in a layout corresponding to this rule. This gives the container a chance
     * to describe the child's layout attributes or other relevant information.
     * <p/>
     * Note that this is called only for single selections.
     * <p/>
     *
     * @param parentNode The parent of the node selected. Never null.
     * @param childNode The child node that was selected. Never null.
     * @return a list of strings to be displayed, or null or empty to display nothing
     */
    List<String> getSelectionHint(INode parentNode, INode childNode);

    /**
     * Paints any layout-specific selection feedback for the given parent layout.
     *
     * @param graphics the graphics context to paint into
     * @param parentNode the parent layout node
     * @param childNodes the child nodes selected in the parent layout
     */
    void paintSelectionFeedback(IGraphics graphics, INode parentNode,
            List<? extends INode> childNodes);

    // ==== Drag'n'drop support ====

    /**
     * Called when the d'n'd starts dragging over the target node.
     * If interested, returns a DropFeedback passed to onDrop/Move/Leave/Paint.
     * If not interested in drop, return null.
     * Followed by a paint.
     */
    DropFeedback onDropEnter(INode targetNode,
            IDragElement[] elements);

    /**
     * Called after onDropEnter.
     * Returns a DropFeedback passed to onDrop/Move/Leave/Paint (typically same
     * as input one).
     * Returning null will invalidate the drop workflow.
     */
    DropFeedback onDropMove(INode targetNode,
            IDragElement[] elements,
            DropFeedback feedback,
            Point where);

    /**
     * Called when drop leaves the target without actually dropping.
     * <p/>
     * When switching between views, onDropLeave is called on the old node *after* onDropEnter
     * is called after a new node that returned a non-null feedback. The feedback received here
     * is the one given by the previous onDropEnter on the same target.
     * <p/>
     * E.g. call order is:
     * <pre>
     * - onDropEnter(node1) => feedback1
     * <i>...user moves to new view...</i>
     * - onDropEnter(node2) => feedback2
     * - onDropLeave(node1, feedback1)
     * <i>...user leaves canvas...</i>
     * - onDropLeave(node2, feedback2)
     * </pre>
     */
    void onDropLeave(INode targetNode,
            IDragElement[] elements,
            DropFeedback feedback);

    /**
     * Called when drop is released over the target to perform the actual drop.
     * <p>
     * TODO: Document that this method will be called under an edit lock so you can
     * directly manipulate the nodes without wrapping it in an
     * {@link INode#editXml(String, INodeHandler)} call
     */
    void onDropped(INode targetNode,
            IDragElement[] elements,
            DropFeedback feedback,
            Point where);

    /**
     * Called when pasting elements in an existing document on the selected target.
     *
     * @param targetNode The first node selected.
     * @param pastedElements The elements being pasted.
     */
    void onPaste(INode targetNode, IDragElement[] pastedElements);

    // ==== XML Creation ====

    /**
     * Called when a view for this rule is being created. This allows for the rule to
     * customize the newly created object. Note that this method is called not just when a
     * view is created from a palette drag, but when views are constructed via a drag-move
     * (where views are created in the destination and then deleted from the source), and
     * even when views are constructed programmatically from other view rules. The
     * {@link InsertType} parameter can be used to distinguish the context for the
     * insertion. For example, the <code>DialerFilterRule</code> will insert EditText children
     * when a DialerFilter is first created, but not during a copy/paste or a move.
     *
     * @param node the newly created node (which will always be a View that applies to
     *            this {@link IViewRule})
     * @param parent the parent of the node (which may not yet contain the newly created
     *            node in its child list)
     * @param insertType whether this node was created as part of a newly created view, or
     *            as a copy, or as a move, etc.
     */
    void onCreate(INode node, INode parent, InsertType insertType);

    /**
     * Called when a child for this view has been created and is being inserted into the
     * view parent for which this {@link IViewRule} applies. Allows the parent to perform
     * customizations of the object. As with {@link #onCreate}, the {@link InsertType}
     * parameter can be used to handle new creation versus moves versus copy/paste
     * operations differently.
     *
     * @param child the newly created node
     * @param parent the parent of the newly created node (which may not yet contain the
     *            newly created node in its child list)
     * @param insertType whether this node was created as part of a newly created view, or
     *            as a copy, or as a move, etc.
     */
    void onChildInserted(INode child, INode parent, InsertType insertType);

    /**
     * Called when one or more children are about to be deleted by the user. Note that
     * children deleted programmatically from view rules (via
     * {@link INode#removeChild(INode)}) will not notify about deletion.
     * <p>
     * Note that this method will be called under an edit lock, so rules can directly
     * add/remove nodes and attributes as part of the deletion handling (and their
     * actions will be part of the same undo-unit.)
     *
     * @param deleted a nonempty list of children about to be deleted
     * @param parent the parent of the deleted children (which still contains the children
     *            since this method is called before the deletion is performed)
     */
    void onRemovingChildren(List<INode> deleted, INode parent);

    /**
     * Called by the IDE on the parent layout when a child widget is being resized. This
     * is called once at the beginning of the resizing operation. A horizontal edge,
     * or a vertical edge, or both, can be resized simultaneously.
     *
     * @param child the widget being resized
     * @param parent the layout containing the child
     * @param horizEdge The horizontal edge being resized, or null
     * @param verticalEdge the vertical edge being resized, or null
     * @return a {@link DropFeedback} object which performs an update painter callback
     *         etc.
     */
    DropFeedback onResizeBegin(INode child, INode parent,
            SegmentType horizEdge, SegmentType verticalEdge);

    /**
     * Called by the IDE on the parent layout when a child widget is being resized. This
     * is called repeatedly during the resize as the mouse is dragged to update the drag
     * bounds, recompute guidelines, etc. The resize has not yet been "committed" so the
     * XML should not be edited yet.
     *
     * @param feedback the {@link DropFeedback} object created in {@link #onResizeBegin}
     * @param child the widget being resized
     * @param parent the layout containing the child
     * @param newBounds the new bounds the user has chosen to resize the widget to,
     *    in absolute coordinates
     * @param modifierMask The modifier keys currently pressed by the user, as a bitmask
     *    of the constants {@link DropFeedback#MODIFIER1}, {@link DropFeedback#MODIFIER2}
     *    and {@link DropFeedback#MODIFIER3}.
     */
    void onResizeUpdate(DropFeedback feedback, INode child, INode parent, Rect newBounds,
            int modifierMask);

    /**
     * Called by the IDE on the parent layout when a child widget is being resized. This
     * is called once at the end of the resize operation, if it was not canceled.
     * This method can call {@link INode#editXml} to update the node to reflect the
     * new bounds.
     *
     * @param feedback the {@link DropFeedback} object created in {@link #onResizeBegin}
     * @param child the widget being resized
     * @param parent the layout containing the child
     * @param newBounds the new bounds the user has chosen to resize the widget to,
     *    in absolute coordinates
     */
    void onResizeEnd(DropFeedback feedback, INode child, INode parent, Rect newBounds);
}
