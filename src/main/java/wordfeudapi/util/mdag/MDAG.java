/**
 * mdag is a Java library capable of constructing character-sequence-storing,
 * directed acyclic graphs of minimal size. 
 * 
 *  Copyright (C) 2012 Kevin Lawson <Klawson88@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wordfeudapi.util.mdag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * A minimalistic directed acyclical graph suitable for storing a set of Strings.
 
 * @author Kevin
 */
public class MDAG
{
    //MDAGNode from which all others in the structure are reachable (all manipulation and non-simplified mdag search operations begin from this).
    private MDAGNode sourceNode = new MDAGNode(false);

    //SimpleMDAGNode from which all others in the structure are reachable (will be defined if this mdag is simplified)
    private SimpleMDAGNode simplifiedSourceNode;

    //HashMap which contains the MDAGNodes collectively representing the all unique equivalence classes in the mdag.
    //Uniqueness is defined by the types of transitions allowed from, and number and type of nodes reachable
    //from the node of interest. Since there are no duplicate nodes in an mdag, # of equivalence classes == # of nodes.
    private HashMap<MDAGNode, MDAGNode> equivalenceClassMDAGNodeHashMap = new HashMap<MDAGNode, MDAGNode>();

    //Array that will contain a space-saving version of the mdag after a call to simplify().
    private SimpleMDAGNode[] mdagDataArray;

    //HashSet which will contain the set of unique characters used as transition labels in the mdag
    private TreeSet<Character> charTreeSet = new TreeSet<Character>();

    //An int denoting the total number of transitions between the nodes of the mdag
    private int transitionCount;

    //Enum containing fields collectively denoting the set of all conditions that can be applied to a search on the mdag
    private static enum SearchCondition {
        NO_SEARCH_CONDITION, PREFIX_SEARCH_CONDITION, SUBSTRING_SEARCH_CONDITION, SUFFIX_SEARCH_CONDITION;

        /**
        * Determines whether two Strings have a given type of relationship.

        * @param processingString      a String
        * @param conditionString       a String
        * @param searchCondition       an int denoting the type of condition to be satisfied
        * @return                      true if {@code processingString} has a relationship with
        *                              {@code conditionString} described by the condition
        *                              represented by {@code searchCondition}
        */
        public boolean satisfiesCondition(String str1, String str2)
        {
            boolean satisfiesSearchCondition;

            switch(this)
            {
                case PREFIX_SEARCH_CONDITION:
                    satisfiesSearchCondition = (str1.startsWith(str2));
                    break;
                case SUBSTRING_SEARCH_CONDITION:
                    satisfiesSearchCondition = (str1.contains(str2));
                    break;
                case SUFFIX_SEARCH_CONDITION:
                    satisfiesSearchCondition = (str1.endsWith(str2));
                    break;
                default:
                    satisfiesSearchCondition = true;
                    break;
            }

            return satisfiesSearchCondition;
        }
    };
    /////



    /**
     * Creates an mdag from a newline delimited file containing the data of interest.

     * @param dataFile          a {@link File} representation of a file
     *                          containing the Strings that the mdag will contain
     * @throws IOException      if {@code datafile} cannot be opened, or a read operation on it cannot be carried out
     */
    public MDAG(File dataFile) throws IOException
    {
        BufferedReader dataFileBufferedReader = new BufferedReader(new FileReader(dataFile));
        String currentString = "";
        String previousString = "";

        //Read all the lines in dataFile and add the String contained in each to the mdag.
        while((currentString = dataFileBufferedReader.readLine()) != null)
        {
            int mpsIndex = calculateMinimizationProcessingStartIndex(previousString, currentString);

            //If the transition path of the previousString needs to be examined for minimization or
            //equivalence class representation after a certain point, call replaceOrRegister to do so.
            if(mpsIndex != -1)
            {
                String transitionSubstring = previousString.substring(0, mpsIndex);
                String minimizationProcessingSubstring = previousString.substring(mpsIndex);
                replaceOrRegister(sourceNode.transition(transitionSubstring), minimizationProcessingSubstring);
            }
            /////

            addStringInternal(currentString);
            previousString = currentString;
        }
        /////

        //Since we delay the minimization of the previously-added String
        //until after we read the next one, we need to have a seperate
        //statement to minimize the absolute last String.
        replaceOrRegister(sourceNode, previousString);
    }



     /**
     * Creates an mdag from a collection of Strings.

     * @param strCollection     a {@link Collection} containing Strings that the mdag will contain
     */
    public MDAG(Collection<String> strCollection)
    {
        addStrings(strCollection);
    }



    /**
     * Adds a Collection of Strings to the mdag.

     * @param strCollection     a {@link Collection} containing Strings to be added to the mdag
     */
    public final void addStrings(Collection<String> strCollection)
    {
        if(sourceNode != null)
        {
            String previousString = "";

            //Add all the Strings in strCollection to the mdag.
            for(String currentString : strCollection)
            {
                int mpsIndex = calculateMinimizationProcessingStartIndex(previousString, currentString);

                //If the transition path of the previousString needs to be examined for minimization or
                //equivalence class representation after a certain point, call replaceOrRegister to do so.
                if(mpsIndex != -1)
                {

                    String transitionSubstring = previousString.substring(0, mpsIndex);
                    String minimizationProcessingSubString = previousString.substring(mpsIndex);
                    replaceOrRegister(sourceNode.transition(transitionSubstring), minimizationProcessingSubString);
                }
                /////

                addStringInternal(currentString);
                previousString = currentString;
            }
            /////

            //Since we delay the minimization of the previously-added String
            //until after we read the next one, we need to have a seperate
            //statement to minimize the absolute last String.
            replaceOrRegister(sourceNode, previousString);
        }
        else
            throw new UnsupportedOperationException("mdag is simplified. Unable to add additional Strings.");
    }



    /**
     * Adds a string to the mdag.

     * @param str       the String to be added to the mdag
     */
    public void addString(String str)
    {
        if(sourceNode != null)
        {
            addStringInternal(str);
            replaceOrRegister(sourceNode, str);
        }
        else
            throw new UnsupportedOperationException("mdag is simplified. Unable to add additional Strings.");
    }



    private void splitTransitionPath(MDAGNode originNode, String storedStringSubstr)
    {
        HashMap<String, Object> firstConfluenceNodeDataHashMap = getTransitionPathFirstConfluenceNodeData(originNode, storedStringSubstr);
        Integer toFirstConfluenceNodeTransitionCharIndex = (Integer)firstConfluenceNodeDataHashMap.get("toConfluenceNodeTransitionCharIndex");
        MDAGNode firstConfluenceNode = (MDAGNode)firstConfluenceNodeDataHashMap.get("confluenceNode");

        if(firstConfluenceNode != null)
        {
            MDAGNode firstConfluenceNodeParent = originNode.transition(storedStringSubstr.substring(0, toFirstConfluenceNodeTransitionCharIndex));

            MDAGNode firstConfluenceNodeClone = firstConfluenceNode.clone(firstConfluenceNodeParent, storedStringSubstr.charAt(toFirstConfluenceNodeTransitionCharIndex));

            transitionCount += firstConfluenceNodeClone.getOutgoingTransitionCount();

            String unprocessedSubString = storedStringSubstr.substring(toFirstConfluenceNodeTransitionCharIndex + 1);
            splitTransitionPath(firstConfluenceNodeClone, unprocessedSubString);
        }
    }



    /**
     * Calculates the length of the the sub-path in a transition path, that is used only by a given string.

     * @param str       a String corresponding to a transition path from sourceNode
     * @return          an int denoting the size of the sub-path in the transition path
     *                  corresponding to {@code str} that is only used by {@code str}
     */
    private int calculateSoleTransitionPathLength(String str)
    {
        Stack<MDAGNode> transitionPathNodeStack = sourceNode.getTransitionPathNodes(str);
        transitionPathNodeStack.pop();  //The MDAGNode at the top of the stack is not needed
                                        //(we are processing the outgoing transitions of nodes inside str's transition path,
                                        //the outgoing transitions of the MDAGNode at the top of the stack are outside this path)

        transitionPathNodeStack.trimToSize();

        //Process each node in transitionPathNodeStack, using each to determine whether the
        //transition path corresponding to str is only used by str.  This is true if and only if
        //each node in the transition path has a single outgoing transition and is not an accept state.
        while(!transitionPathNodeStack.isEmpty())
        {
            MDAGNode currentNode = transitionPathNodeStack.peek();
            if(currentNode.getOutgoingTransitions().size() <= 1 && !currentNode.isAcceptNode())
                transitionPathNodeStack.pop();
            else
                break;
        }
        /////

        return (transitionPathNodeStack.capacity() - transitionPathNodeStack.size());
    }



    /**
     * Removes a String from the mdag.

     * @param str       the String to be removed from the mdag
     */
    public void removeString(String str)
    {
        if(sourceNode != null)
        {
            //Split the transition path corresponding to str to ensure that
            //any other transition paths sharing nodes with it are not affected
            splitTransitionPath(sourceNode, str);

            //Remove from equivalenceClassMDAGNodeHashMap, the entries of all the nodes in the transition path corresponding to str.
            removeTransitionPathRegisterEntries(str);

            //Get the last node in the transition path corresponding to str
            MDAGNode strEndNode = sourceNode.transition(str);

            if(!strEndNode.hasOutgoingTransitions())
            {
                int soleInternalTransitionPathLength = calculateSoleTransitionPathLength(str);
                int internalTransitionPathLength = str.length() - 1;

                if(soleInternalTransitionPathLength == internalTransitionPathLength)
                {
                    sourceNode.removeOutgoingTransition(str.charAt(0));
                    transitionCount -= str.length();
                }
                else
                {
                    //Remove the sub-path in str's transition path that is only used by str
                    int toBeRemovedTransitionLabelCharIndex = (internalTransitionPathLength - soleInternalTransitionPathLength);
                    MDAGNode latestNonSoloTransitionPathNode = sourceNode.transition(str.substring(0, toBeRemovedTransitionLabelCharIndex));
                    latestNonSoloTransitionPathNode.removeOutgoingTransition(str.charAt(toBeRemovedTransitionLabelCharIndex));
                    transitionCount -= str.substring(toBeRemovedTransitionLabelCharIndex).length();
                    /////

                    replaceOrRegister(sourceNode, str.substring(0, toBeRemovedTransitionLabelCharIndex));
                }

            }
            else
            {
                strEndNode.setAcceptStateStatus(false);
                replaceOrRegister(sourceNode, str);
            }
        }
        else
            throw new UnsupportedOperationException("mdag is simplified. Unable to remove any Strings.");
    }



    /**
     * Determines the start index of the substring in the String most recently added to the mdag
     * that corresponds to the transition path that will be next up for minimization processing.
     *
     * The "minimization processing start index" is defined as the index in {@code prevStr} which starts the substring
     * corresponding to the transition path that doesn't have its right language extended by {@code currStr}. The transition path of
     * the substring before this point is not considered for minimization in order to limit the amount of times the
     * equivalence classes of its nodes will need to be reassigned during the processing of Strings which share prefixes.

     * @param prevStr       the String most recently added to the mdag
     * @param currStr       the String next to be added to the mdag
     * @return              an int of the index in {@code prevStr} that starts the substring corresponding
     *                      to the transition path next up for minimization processing
     */
    public int calculateMinimizationProcessingStartIndex(String prevStr, String currStr)
    {
        int mpsIndex;

        if(!currStr.startsWith(prevStr))
        {
            //Loop through the corresponding indices of both Strings in search of the first index containing differing characters.
            //The transition path of the substring of prevStr from this point will need to be submitted for minimization processing.
            //The substring before this point, however, does not, since currStr will simply be extending the right languages of the
            //nodes on its transition path.
            int shortestStringLength = Math.min(prevStr.length(), currStr.length());
            for(mpsIndex = 0; mpsIndex < shortestStringLength && prevStr.charAt(mpsIndex) == currStr.charAt(mpsIndex); mpsIndex++){};
            /////
        }
        else
            mpsIndex =  -1;    //If the prevStr is a prefix of currStr, then currStr simply extends the right language of the transition path of prevStr.

        return mpsIndex;
    }



    /**
     * Determines the longest prefix of a given String that is
     * the prefix of another String previously added to the mdag.

     * @param str       the String to be processed
     * @return          a String of the longest prefix of {@code str}
     *                  that is also a prefix of a String contained in the mdag
     */
    public String determineLongestPrefixInMDAG(String str)
    {
        MDAGNode currentNode = sourceNode;
        int numberOfChars = str.length();
        int onePastPrefixEndIndex = 0;

        //Loop through the characters in str, using them in sequence to transition
        //through the mdag until the currently processing node doesn't have a transition
        //labeled with the current processing char, or there are no more characters to process.
        for(int i = 0; i < numberOfChars; i++, onePastPrefixEndIndex++)
        {
            char currentChar = str.charAt(i);
            if(currentNode.hasOutgoingTransition(currentChar))
                currentNode = currentNode.transition(currentChar);
            else
                break;
        }
        /////

        return str.substring(0, onePastPrefixEndIndex);
    }



    /**
     * Determines and retrieves data related to the first confluence node
     * (defined as a node with two or more incoming transitions) of a
     * transition path corresponding to a given String from a given node.

     * @param originNode        the MDAGNode from which the transition path corresponding to str starts from
     * @param str               a String corresponding to a transition path in the mdag
     * @return                  a HashMap of Strings to Objects containing:
     *                              - an int denoting the length of the path to the first confluence node in the transition path of interest
     *                              - the MDAGNode which is the first confluence node in the transition path of interest (or null if one does not exist)
     */
    public HashMap<String, Object> getTransitionPathFirstConfluenceNodeData(MDAGNode originNode, String str)
    {
        int currentIndex = 0;
        int charCount = str.length();
        MDAGNode currentNode = originNode;

        //Loop thorugh the characters in str, sequentially using them to transition through the mdag in search of
        //(and breaking upon reaching) the first node that is the target of two or more transitions. The loop is
        //also broken from if the currently processing node doesn't have a transition labeled with the currently processing char.
        for(; currentIndex < charCount; currentIndex++)
        {
            char currentChar = str.charAt(currentIndex);
            currentNode = (currentNode.hasOutgoingTransition(currentChar) ? currentNode.transition(currentChar) : null);

            if(currentNode == null || currentNode.isConfluenceNode())
                break;
        }
        /////

        boolean noConfluenceNode = (currentNode == originNode  || currentIndex == charCount);

        //Create a HashMap containing the index of the last char in the substring corresponding
        //to the transitoin path to the confluence node, as well as the actual confluence node
        HashMap<String, Object> confluenceNodeDataHashMap = new HashMap<String, Object>(2);
        confluenceNodeDataHashMap.put("toConfluenceNodeTransitionCharIndex", (noConfluenceNode ? null : currentIndex));
        confluenceNodeDataHashMap.put("confluenceNode", noConfluenceNode ? null : currentNode);
        /////

        return confluenceNodeDataHashMap;
    }



    /**
     * Performs minimization processing on a transition path starting from a given node.
     *
     * This entails either replacing a node in the path with one that has an equivalent right language/equivalence class
     * (defined as set of transition paths that can be traversed and nodes able to be reached from it), or making it
     * a representative of a right language/equivalence class if a such a node does not already exist.

     * @param originNode        the MDAGNode that the transition path corresponding to str starts from
     * @param str              a String related to a transition path
     */
    private void replaceOrRegister(MDAGNode originNode, String str)
    {
        char transitionLabelChar = str.charAt(0);
        MDAGNode relevantTargetNode = originNode.transition(transitionLabelChar);

        //If relevantTargetNode has transitions and there is at least one char left to process, recursively call
        //this on the next char in order to further processing down the transition path corresponding to str
        if(relevantTargetNode.hasOutgoingTransitions() && !str.substring(1).isEmpty())
            replaceOrRegister(relevantTargetNode, str.substring(1));
        /////

        //Get the node representing the equivalence class that relevantTargetNode belongs to. MDAGNodes hash on the
        //transitions paths that can be traversed from them and nodes able to be reached from them;
        //nodes with the same equivalence classes will hash to the same bucket.
        MDAGNode equivalentNode = equivalenceClassMDAGNodeHashMap.get(relevantTargetNode);

        if(equivalentNode == null)  //if there is no node with the same right language as relevantTargetNode
            equivalenceClassMDAGNodeHashMap.put(relevantTargetNode, relevantTargetNode);
        else if(equivalentNode != relevantTargetNode)   //if there is another node with the same right language as relevantTargetNode, reassign the
        {                                               //transition between originNode and relevantTargetNode, to originNode and the node representing the equivalence class of interest
            relevantTargetNode.decrementTargetIncomingTransitionCounts();
            transitionCount -= relevantTargetNode.getOutgoingTransitionCount(); //Since this method is recursive, the outgoing transitions of all of relevantTargetNode's child nodes have already been reassigned,
                                                                                    //so we only need to decrement the transition count by the relevantTargetNode's outgoing transition count
            originNode.reassignOutgoingTransition(transitionLabelChar, relevantTargetNode, equivalentNode);
        }
    }



    /**
     * Adds a transition path starting from a specific node in the mdag.

     * @param originNode    the MDAGNode which will serve as the start point of the to-be-created transition path
     * @param str           the String to be used to create a new transition path from {@code originNode}
     */
    private void addTransitionPath(MDAGNode originNode, String str)
    {
        if(!str.isEmpty())
        {
            MDAGNode currentNode = originNode;
            int charCount = str.length();

            //Loop through the characters in str, iteratevely adding
            // a transition path corresponding to it from originNode
            for(int i = 0; i < charCount; i++, transitionCount++)
            {
                char currentChar = str.charAt(i);
                boolean isLastChar = (i == charCount - 1);
                currentNode = currentNode.addOutgoingTransition(currentChar, isLastChar);

                charTreeSet.add(currentChar);
            }
            /////
        }
        else
            originNode.setAcceptStateStatus(true);
    }



    /**
     * Removes from equivalenceClassMDAGNodeHashmap the entries of all the nodes in a transition path.

     * @param str       a String corresponding to a transition path from sourceNode
     */
    private void removeTransitionPathRegisterEntries(String str)
    {
        MDAGNode currentNode = sourceNode;

        int charCount = str.length();

        for(int i = 0; i < charCount; i++)
        {
            currentNode = currentNode.transition(str.charAt(i));
            if(equivalenceClassMDAGNodeHashMap.get(currentNode) == currentNode)
                equivalenceClassMDAGNodeHashMap.remove(currentNode);

            //The hashCode of an MDAGNode is cached the first time a hash is performed without a cache value present.
            //Since we just hashed currentNode, we must clear this regardless of its presence in equivalenceClassMDAGNodeHashMap
            //since we're not actually declaring equivalence class representatives here.
            currentNode.clearStoredHashCode();
        }
    }



    /**
     * Clones a transition path from a given node.

     * @param pivotConfluenceNode               the MDAGNode that the cloning operation is to be based from
     * @param transitionStringToPivotNode       a String which corresponds with a transition path from souceNode to {@code pivotConfluenceNode}
     * @param str                               a String which corresponds to the transition path from {@code pivotConfluenceNode} that is to be cloned
     */
    private void cloneTransitionPath(MDAGNode pivotConfluenceNode, String transitionStringToPivotNode, String str)
    {
        MDAGNode lastTargetNode = pivotConfluenceNode.transition(str);      //Will store the last node which was used as the base of a cloning operation
        MDAGNode lastClonedNode = null;                                     //Will store the last cloned node
        char lastTransitionLabelChar = '\0';                                //Will store the char which labels the transition to lastTargetNode from its parent node in the prefixString's transition path

        //Loop backwards through the indices of str, using each as a boundary to create substrings of str of decreasing length
        //which will be used to transition to, and duplicate the nodes in the transition path of str from pivotConfluenceNode.
        for(int i = str.length(); i >= 0; i--)
        {
            String currentTransitionString = (i > 0 ? str.substring(0, i) : null);
            MDAGNode currentTargetNode = (i > 0 ? pivotConfluenceNode.transition(currentTransitionString) : pivotConfluenceNode) ;
            MDAGNode clonedNode;

            if(i == 0)  //if we have reached pivotConfluenceNode
            {
                //Clone pivotConfluenceNode in a way that reassigns the transition of its parent node (in transitionStringToConfluenceNode's path) to the clone.
                String transitionStringToPivotNodeParent = transitionStringToPivotNode.substring(0, transitionStringToPivotNode.length() - 1);
                char parentTransitionLabelChar = transitionStringToPivotNode.charAt(transitionStringToPivotNode.length() - 1);
                clonedNode = pivotConfluenceNode.clone(sourceNode.transition(transitionStringToPivotNodeParent), parentTransitionLabelChar);
                /////
            }
            else
                clonedNode = currentTargetNode.clone();     //simply clone curentTargetNode

            transitionCount += clonedNode.getOutgoingTransitionCount();

            //If this isn't the first node we've cloned, reassign clonedNode's transition labeled
            //with the lastTransitionChar (which points to the last targetNode) to the last clone.
            if(lastClonedNode != null)
            {
                clonedNode.reassignOutgoingTransition(lastTransitionLabelChar, lastTargetNode, lastClonedNode);
                lastTargetNode = currentTargetNode;
            }

            //Store clonedNode and the char which labels the transition between the node it was cloned from (currentTargetNode) and THAT node's parent.
            //These will be used to establish an equivalent transition to clonedNode from the next clone to be created (it's clone parent).
            lastClonedNode = clonedNode;
            lastTransitionLabelChar = (i > 0 ? str.charAt(i - 1) : '\0');
            /////
        }
        /////
    }



    /**
     * Adds a String to the mdag (called by addString to do actual mdag manipulation).

     * @param str       the String to be added to the mdag
     */
    private void addStringInternal(String str)
    {
        String prefixString = determineLongestPrefixInMDAG(str);
        String suffixString = str.substring(prefixString.length());

        //Retrive the data related to the first confluence node (a node with two or more incoming transitions)
        //in the transition path from sourceNode corresponding to prefixString.
        HashMap<String, Object> firstConfluenceNodeDataHashMap = getTransitionPathFirstConfluenceNodeData(sourceNode, prefixString);
        MDAGNode firstConfluenceNodeInPrefix = (MDAGNode)firstConfluenceNodeDataHashMap.get("confluenceNode");
        Integer toFirstConfluenceNodeTransitionCharIndex = (Integer) firstConfluenceNodeDataHashMap.get("toConfluenceNodeTransitionCharIndex");
        /////

        //Remove the register entries of all the nodes in the prefixString transition path up to the first confluence node
        //(those past the confluence node will not need to be removed since they will be cloned and unaffected by the
        //addition of suffixString). If there is no confluence node in prefixString, then remove the register entries in prefixString's entire transition path
        removeTransitionPathRegisterEntries((toFirstConfluenceNodeTransitionCharIndex == null ? prefixString : prefixString.substring(0, toFirstConfluenceNodeTransitionCharIndex)));

        //If there is a confluence node in the prefix, we must duplicate the transition path
        //of the prefix starting from that node, before we add suffixString (to the duplicate path).
        //This ensures that we do not disturb the other transition paths containing this node.
        if(firstConfluenceNodeInPrefix != null)
        {
            String transitionStringOfPathToFirstConfluenceNode = prefixString.substring(0, toFirstConfluenceNodeTransitionCharIndex + 1);
            String transitionStringOfToBeDuplicatedPath = prefixString.substring(toFirstConfluenceNodeTransitionCharIndex + 1);
            cloneTransitionPath(firstConfluenceNodeInPrefix, transitionStringOfPathToFirstConfluenceNode, transitionStringOfToBeDuplicatedPath);
        }
        /////

        //Add the transition based on suffixString to the end of the (possibly duplicated) transition path corresponding to prefixString
        addTransitionPath(sourceNode.transition(prefixString), suffixString);
    }



    /**
     * Creates a SimpleMDAGNode version of an MDAGNode's outgoing transition set in mdagDataArray.

     * @param node                                      the MDAGNode containing the transition set to be inserted in to {@code mdagDataArray}
     * @param mdagDataArray                             an array of SimpleMDAGNodes containing a subset of the data of the mdag
     * @param onePastLastCreatedConnectionSetIndex      an int of the index in {@code mdagDataArray} that the outgoing transition set of {@code node} is to start from
     * @return                                          an int of one past the end of the transition set located farthest in {@code mdagDataArray}
     */
    private int createSimpleMDAGTransitionSet(MDAGNode node, SimpleMDAGNode[] mdagDataArray, int onePastLastCreatedTransitionSetIndex)
    {
        int pivotIndex = onePastLastCreatedTransitionSetIndex;
        node.setTransitionSetBeginIndex(pivotIndex);

        onePastLastCreatedTransitionSetIndex += node.getOutgoingTransitionCount();

        //Create a SimpleMDAGNode representing each transition label/target combo in transitionTreeMap, recursively calling this method (if necessary)
        //to set indices in these SimpleMDAGNodes that the set of transitions emitting from their respective transition targets starts from.
        TreeMap<Character, MDAGNode> transitionTreeMap = node.getOutgoingTransitions();
        for(Entry<Character, MDAGNode> transitionKeyValuePair : transitionTreeMap.entrySet())
        {
            //Use the current transition's label and target node to create a SimpleMDAGNode
            //(which is a space-saving representation of the transition), and insert it in to mdagDataArray
            char transitionLabelChar = transitionKeyValuePair.getKey();
            MDAGNode transitionTargetNode = transitionKeyValuePair.getValue();
            mdagDataArray[pivotIndex] = new SimpleMDAGNode(transitionLabelChar, transitionTargetNode.isAcceptNode(), transitionTargetNode.getOutgoingTransitionCount());
            /////

            //If targetTransitionNode's outgoing transition set hasn't been inserted in to mdagDataArray yet, call this method on it to do so.
            //After this call returns, transitionTargetNode will contain the index in mdagDataArray that its transition set starts from
            if(transitionTargetNode.getTransitionSetBeginIndex() == -1)
                onePastLastCreatedTransitionSetIndex = createSimpleMDAGTransitionSet(transitionTargetNode, mdagDataArray, onePastLastCreatedTransitionSetIndex);

            mdagDataArray[pivotIndex++].setTransitionSetBeginIndex(transitionTargetNode.getTransitionSetBeginIndex());
        }
        /////

        return onePastLastCreatedTransitionSetIndex;
    }



    /**
     * Creates a space-saving version of the mdag in the form of an array.
     * Once the mdag is simplified, Strings can no longer be added to or removed from it.
     */
    public void simplify()
    {
        if(sourceNode != null)
        {
            mdagDataArray = new SimpleMDAGNode[transitionCount];
            createSimpleMDAGTransitionSet(sourceNode, mdagDataArray, 0);
            simplifiedSourceNode = new SimpleMDAGNode('\0', false, sourceNode.getOutgoingTransitionCount());

            //Mark the previous mdag data structure and equivalenceClassMDAGNodeHashMap
            //for garbage collection since they are no longer needed.
            sourceNode = null;
            equivalenceClassMDAGNodeHashMap = null;
            /////
        }
    }



    /**
     * Determines whether a String is present in the mdag.

     * @param str       the String to be searched for
     * @return          true if {@code str} is present in the mdag, and false otherwise
     */
    public boolean contains(String str)
    {
        if(sourceNode != null)      //if the mdag hasn't been simplified
        {
            MDAGNode targetNode = sourceNode.transition(str);
            return (targetNode != null && targetNode.isAcceptNode());
        }
        else
        {
            SimpleMDAGNode targetNode = SimpleMDAGNode.traverseMDAG(mdagDataArray, simplifiedSourceNode, str);
            return(targetNode != null && targetNode.isAcceptNode());
        }
    }



    /**
     * Retrieves Strings corresponding to all valid transition paths from a given node that satisfy a given condition.

     * @param strHashSet                a HashSet of Strings to contain all those in the mdag satisfying
     *                                  {@code searchCondition} with {@code conditionString}
     * @param searchCondition           the SearchCondition enum field describing the type of relationship that Strings contained in the mdag
     *                                  must have with {@code conditionString} in order to be included in the result set
     * @param searchConditionString     the String that all Strings in the mdag must be related with in the fashion denoted
     *                                  by {@code searchCondition} in order to be included in the result set
     * @param prefixString              the String corresponding to the currently traversed transition path
     * @param transitionTreeMap         a TreeMap of Characters to MDAGNodes collectively representing an MDAGNode's transition set
     */
    private void getStrings(HashSet<String> strHashSet, SearchCondition searchCondition, String searchConditionString, String prefixString, TreeMap<Character, MDAGNode> transitionTreeMap)
    {
        //Traverse all the valid transition paths beginning from each transition in transitionTreeMap, inserting the
        //corresponding Strings in to strHashSet that have the relationship with conditionString denoted by searchCondition
        for(Entry<Character, MDAGNode> transitionKeyValuePair : transitionTreeMap.entrySet())
        {
            String newPrefixString = prefixString + transitionKeyValuePair.getKey();
            MDAGNode currentNode = transitionKeyValuePair.getValue();

            if(currentNode.isAcceptNode() && searchCondition.satisfiesCondition(newPrefixString, searchConditionString))
                strHashSet.add(newPrefixString);

            //Recursively call this to traverse all the valid transition paths from currentNode
            getStrings(strHashSet, searchCondition, searchConditionString, newPrefixString, currentNode.getOutgoingTransitions());
        }
        /////
    }



    /**
     * Retrieves Strings corresponding to all valid transition paths from a given node that satisfy a given condition.

     * @param strHashSet                    a HashSet of Strings to contain all those in the mdag satisfying
     *                                      {@code searchCondition} with {@code conditionString}
     * @param searchCondition               the SearchCondition enum field describing the type of relationship that Strings contained in the mdag
     *                                      must have with {@code conditionString} in order to be included in the result set
     * @param searchConditionString         the String that all Strings in the mdag must be related with in the fashion denoted
     *                                      by {@code searchCondition} in order to be included in the result set
     * @param prefixString                  the String corresponding to the currently traversed transition path
     * @param transitionSetBegin            an int denoting the starting index of a SimpleMDAGNode's transition set in mdagDataArray
     * @param onePastTransitionSetEnd       an int denoting one past the last index of a simpleMDAGNode's transition set in mdagDataArray
     */
    private void getStrings(HashSet<String> strHashSet, SearchCondition searchCondition, String searchConditionString, String prefixString, SimpleMDAGNode node)
    {
        int transitionSetBegin = node.getTransitionSetBeginIndex();
        int onePastTransitionSetEnd = transitionSetBegin +  node.getOutgoingTransitionSetSize();

        //Traverse all the valid transition paths beginning from each transition in transitionTreeMap, inserting the
        //corresponding Strings in to strHashSet that have the relationship with conditionString denoted by searchCondition
        for(int i = transitionSetBegin; i < onePastTransitionSetEnd; i++)
        {
            SimpleMDAGNode currentNode = mdagDataArray[i];
            String newPrefixString = prefixString + currentNode.getLetter();

            if(currentNode.isAcceptNode() && searchCondition.satisfiesCondition(newPrefixString, searchConditionString))
                strHashSet.add(newPrefixString);

            //Recursively call this to traverse all the valid transition paths from currentNode
            getStrings(strHashSet, searchCondition, searchConditionString, newPrefixString, currentNode);
        }
        /////
    }



    /**
     * Retrieves all the valid Strings that have been inserted in to the mdag.

     * @return      a HashSet containing all the Strings that have been inserted into the mdag
     */
    public HashSet<String> getAllStrings()
    {
        HashSet<String> strHashSet = new HashSet<String>();

        if(sourceNode != null)
            getStrings(strHashSet, SearchCondition.NO_SEARCH_CONDITION, null, "", sourceNode.getOutgoingTransitions());
        else
            getStrings(strHashSet, SearchCondition.NO_SEARCH_CONDITION, null, "", simplifiedSourceNode);

        return strHashSet;
    }



    /**
     * Retrieves all the Strings in the mdag that begin with a given String.

     * @param prefixStr     a String that is the prefix for all the desired Strings
     * @return              a HashSet containing all the Strings present in the mdag that begin with {@code prefixString}
     */
    public HashSet<String> getStringsStartingWith(String prefixStr)
    {
        HashSet<String> strHashSet = new HashSet<String>();

        if(sourceNode != null)      //if the mdag hasn't been simplified
        {
            MDAGNode originNode = sourceNode.transition(prefixStr);  //attempt to transition down the path denoted by prefixStr

            if(originNode != null) //if there a transition path corresponding to prefixString (one or more stored Strings begin with prefixString)
            {
                if(originNode.isAcceptNode()) strHashSet.add(prefixStr);
                getStrings(strHashSet, SearchCondition.PREFIX_SEARCH_CONDITION, prefixStr, prefixStr, originNode.getOutgoingTransitions());   //retrieve all Strings that extend the transition path denoted by prefixStr
            }
        }
        else
        {
            SimpleMDAGNode originNode = SimpleMDAGNode.traverseMDAG(mdagDataArray, simplifiedSourceNode, prefixStr);      //attempt to transition down the path denoted by prefixStr

            if(originNode != null)      //if there a transition path corresponding to prefixString (one or more stored Strings begin with prefixStr)
            {
                if(originNode.isAcceptNode()) strHashSet.add(prefixStr);
                getStrings(strHashSet, SearchCondition.PREFIX_SEARCH_CONDITION, prefixStr, prefixStr, originNode);        //retrieve all Strings that extend the transition path denoted by prefixString
            }
        }

        return strHashSet;
    }



    /**
     * Retrieves all the Strings in the mdag that contain a given String.

     * @param str       a String that is contained in all the desired Strings
     * @return          a HashSet containing all the Strings present in the mdag that begin with {@code prefixString}
     */
    public HashSet<String> getStringsWithSubstring(String str)
    {
        HashSet<String> strHashSet = new HashSet<String>();

        if(sourceNode != null)      //if the mdag hasn't been simplified
            getStrings(strHashSet, SearchCondition.SUBSTRING_SEARCH_CONDITION, str, "", sourceNode.getOutgoingTransitions());
        else
            getStrings(strHashSet, SearchCondition.SUBSTRING_SEARCH_CONDITION, str, "", simplifiedSourceNode);

        return strHashSet;
    }



     /**
     * Retrieves all the Strings in the mdag that begin with a given String.

     * @param suffixString      a String that is the suffix for all the desired Strings
     * @return                  a HashSet containing all the Strings present in the mdag that end with {@code suffixStr}
     */
    public HashSet<String> getStringsEndingWith(String suffixStr)
    {
        HashSet<String> strHashSet = new HashSet<String>();

        if(sourceNode != null)      //if the mdag hasn't been simplified
            getStrings(strHashSet, SearchCondition.SUFFIX_SEARCH_CONDITION, suffixStr, "", sourceNode.getOutgoingTransitions());
        else
            getStrings(strHashSet, SearchCondition.SUFFIX_SEARCH_CONDITION, suffixStr, "", simplifiedSourceNode);

         return strHashSet;
    }



    /**
     * Returns the mdag's source node.

     * @return      the MDAGNode or SimpleMDAGNode functioning as the mdag's source node.
     */
    public Object getSourceNode()
    {
        return (sourceNode != null ? sourceNode : simplifiedSourceNode);
    }



    /**
     * Returns the array of SimpleMDAGNodes collectively containing the
     * data of this mdag, or null if it hasn't been simplified yet.

     * @return      the array of SimpleMDAGNodes collectively containing the data of this mdag
     *              if this mdag has been simplified, or null if it has not
     */
    public SimpleMDAGNode[] getSimpleMDAGArray()
    {
        return mdagDataArray;
    }



    /**
     * Procures the set of characters which collectively label the mdag's transitions.

     * @return      a TreeSet of chars which collectively label all the transitions in the mdag
     */
    public TreeSet<Character> getTransitionLabelSet()
    {
        return charTreeSet;
    }



    /**
     * Determines if a child node object is accepting.

     * @param nodeObj                       an Object
     * @return                              if {@code nodeObj} is either an MDAGNode or a SimplifiedMDAGNode,
     *                                      true if the node is accepting, false otherwise
     * throws IllegalArgumentException      if {@code nodeObj} is not an MDAGNode or SimplifiedMDAGNode
     */
    public static boolean isAcceptNode(Object nodeObj)
    {
        if(nodeObj != null)
        {
            Class nodeObjClass = nodeObj.getClass();

            if(nodeObjClass.equals(MDAGNode.class))
                return ((MDAGNode)nodeObj).isAcceptNode();
            else if(nodeObjClass.equals(SimpleMDAGNode.class))
                return ((SimpleMDAGNode)nodeObj).isAcceptNode();

        }

        throw new IllegalArgumentException("Argument is not an MDAGNode or SimpleMDAGNode");
    }

     private int countNodes(MDAGNode originNode, HashSet<Integer> nodeIDHashSet)
    {
        if(originNode != sourceNode) nodeIDHashSet.add(originNode.id);

        TreeMap<Character, MDAGNode> transitionTreeMap = originNode.getOutgoingTransitions();
        
        for(Entry<Character, MDAGNode> transitionKeyValuePair : transitionTreeMap.entrySet())
            countNodes(transitionKeyValuePair.getValue(), nodeIDHashSet);

        return nodeIDHashSet.size();
    }
    
    
    
    
    public int getNodeCount()
    {
        return countNodes(sourceNode, new HashSet<Integer>());
    }
    
    public int getEquivalenceClassCount()
    {
        return equivalenceClassMDAGNodeHashMap.size();
    }
    
    public int getTransitionCount()
    {
        return transitionCount;
    }
}