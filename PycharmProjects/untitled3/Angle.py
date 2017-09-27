import numpy as np
import math
import sys
import cProfile, pstats, io
pr = cProfile.Profile()
pr.enable()

num_triangles, num_vertices = input().split()
n = int(num_triangles)  # amount of triangles
m = int(num_vertices)  # amount of vertices
ind = []
coord = []
angles_list =[]
list_angle_norm = []

# for i in range(n):
#     ind.append([int(j) for j in input().split()])  # indices of the vertices given
edge = {}

for i in range(n):
    a, b, c = [int(j) for j in input().split()]
    ind.append((a, b, c))

    k = (min(a, b), max(a, b))
    edge[k] = edge.get(k, []) + [i]

    k = (min(b, c), max(b, c))
    edge[k] = edge.get(k, []) + [i]

    k = (min(c, a), max(c, a))
    edge[k] = edge.get(k, []) + [i]

for j in range(m):
    coord.append([float(k) for k in input().split()])  # coordinates of the vertices given
edge_list =[]

for key in edge:
    f = len(edge[key])
    if(f >= 2):
      edge_list.append(edge[key])

def unit_vector(v):
    # Returns the unit vector of the vector.
    return v/ np.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2])


def angle_between(v1, v2):
    v1_u = unit_vector(v1)
    v2_u = unit_vector(v2)
    return math.acos(max(min(np.dot(v1_u, v2_u), 1), -1))

def calculate_angle():
    lenL = len(edge_list)
    for k in range(0, lenL):
        index1z = edge_list[k][0]
        index2z = edge_list[k][1]
        n1 = list_angle_norm[index1z]
        n2 = list_angle_norm[index2z]
        ang = angle_between(n1, n2)
        angles_list.append(ang)
    return max(angles_list)


def cross(a, b):
    c = [a[1]*b[2] - a[2]*b[1],
         a[2]*b[0] - a[0]*b[2],
         a[0]*b[1] - a[1]*b[0]]
    return c

def angle_norm(triangle_index):
    p0 = coord[triangle_index[0]]
    p1 = coord[triangle_index[1]]
    p2 = coord[triangle_index[2]]
    V1 = np.array(p1) - np.array(p0)
    V2 = np.array(p2) - np.array(p0)
    an = cross(V1, V2)
    return an

def cr_list_angle_norm():
  for zk in range(0, n):
    num =ind[zk]
    res = angle_norm(num)
    list_angel_norm.append(res)

cr_list_angle_norm()

print(calculate_angle())

pr.disable()
s = io.StringIO()
sortby = 'tottime'
ps = pstats.Stats(pr, stream=s).sort_stats(sortby)
ps.print_stats()
print(s.getvalue())