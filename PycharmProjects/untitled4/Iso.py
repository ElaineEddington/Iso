n = int(input())
parent1 = [int(item) for item in input().split()]
parent2 = [int(item) for item in input().split()]


#Structure to store information about nodes
class TreeNode:
    def __init__(self, data, left=None, right=None):
        self.data = data
        self.left = left
        self.right = right

    def add_child(self, node):
        if not self.left:
            self.left = node
        elif not self.right:
            self.right = node

    def __repr__(self):
        return 'TreeNode({self.data!r}, {self.left!r}, {self.right!r})'.format(self=self)



 # Function which converts trees from parent array representation into the usual one.
def construct_tree(parents: list):

    # Put Nodes with corresponding values into the list
    constructed = [TreeNode(i) for i in range(len(parents))]

    root = None
    for i, parent in enumerate(parents):

        # If parent's index = -1, it's the root of the tree
        if parent == -1:
            root = constructed[i]
        else:
            # Tie up current node to corresponding parent
            constructed[parent].add_child(constructed[i])

    return root


def are_isomorphic(T1, T2):
    # Both roots are empty, trees are isomorphic by default

    if T1 is None and T2 is None:
        return True
    #if T1.data != T2.data

    # If one of the trees is empty, and the other - isn't, do not bother to check further.
    if T1 is None or T2 is None:
        return False

    # There are two possible cases for n1 and n2 to be isomorphic
    # 1: The subtrees rooted at these nodes haven't been swapped
    # 2: The subtrees rooted at these nodes have been swapped

    return (are_isomorphic(T1.left, T2.left) and are_isomorphic(T1.right, T2.right) or
             are_isomorphic(T1.left, T2.right) and are_isomorphic(T1.right, T2.left))



#build trees from parents' arrays in order to compare
Tree1 = construct_tree(parent1)
Tree2 = construct_tree(parent2)

print(are_isomorphic(Tree1,Tree2))