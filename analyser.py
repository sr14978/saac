
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

window_n = 2
branch_n = 4
bypass_n = 2
unit_n = 7
width_n = 7
order_n = 2
addr_n = 5
renaming_n = 2
load_n = 6

with open('results.out.prev.zero', 'r') as f:
  vals = np.empty(shape=(window_n,branch_n,bypass_n,unit_n,width_n,order_n,addr_n,renaming_n,load_n), dtype=float)
  for window in range(window_n):
    for branch in range(branch_n):
      for bypass in range(bypass_n): 
        for unit in range(unit_n): 
          for width in range(width_n): 
            for order in range(order_n): 
              for addr in range(addr_n): 
                for renaming in range(renaming_n): 
                  for load in range(load_n):
                    vals[window][branch][bypass][unit][width][order][addr][renaming][load] = float(f.readline()[:-1])

def show(n, f, xs):
  ys = [np.max(f(i)) for i in range(n)]
  print(ys)
  plt.scatter(xs, ys, marker='o')
  plt.show()

def show3D(x, z, f, xs, zs):
  fig = plt.figure()
  ax = fig.add_subplot(111, projection='3d')
  ys = [np.max(f(i,j)) for i in range(x) for j in range(z)]
  print(len(ys))
  print(len(xs))
  print(len(zs))
  ax.scatter(xs, zs, ys, marker='o')
  #ax.plot_surface(xs, zs, ys, alpha=0.5)
  plt.show()
  


unit_load = lambda: show3D(unit_n, load_n, lambda i,j: vals[:,:,:,i,...,j], [i for i in range(unit_n) for j in range(load_n)], [i for j in range(unit_n) for i in range(load_n)])
unit_width = lambda: show3D(unit_n, width_n, lambda i,j: vals[:,:,:,i,j,...], [i for i in range(unit_n) for j in range(width_n)], [i for j in range(unit_n) for i in range(width_n)])
unit_width = lambda: show3D(load_n, width_n, lambda i,j: vals[:,:,:,i,j,...], [i for i in range(load_n) for j in range(width_n)], [i for j in range(load_n  ) for i in range(width_n)])

window = lambda: show(window_n, lambda i: vals[i,...], range(window_n))
branch = lambda: show(branch_n, lambda i: vals[:,i,...], range(branch_n))
bypass = lambda: show(bypass_n, lambda i: vals[:,:,i,...], range(bypass_n))
unit = lambda: show(unit_n, lambda i: vals[:,:,:,i,...], [2**i for i in range(unit_n)])
width = lambda: show(width_n, lambda i: vals[:,:,:,:,i,...], [2**i for i in range(width_n)])
order = lambda: show(order_n, lambda i: vals[:,:,:,:,:,i,...], range(order_n))
addr = lambda: show(addr_n, lambda i: vals[:,:,:,:,:,:,i,...], [2**i for i in range(8, 8+addr_n)])
renaming = lambda: show(renaming_n, lambda i: vals[:,:,:,:,:,:,:,i,...], range(renaming_n))
load = lambda: show(load_n, lambda i: vals[:,:,:,:,:,:,:,:,i,...], [2**i for i in range(load_n)])
