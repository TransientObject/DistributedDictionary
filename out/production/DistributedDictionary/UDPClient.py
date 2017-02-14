import socket
 

ip_address = socket.gethostbyname("localhost")
port = 9876
server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

while(True):
  #word = input("Enter a word you want to search meaning for\n")

  #while(not word):
    #word = input("You Entered a empty word. Enter a valid word\n")


  #print(ip_address, port)
  #print("searching meaning for", word)
  words = ["abet", "apple", "hello", "hi", "asdasdas"];
  for word in words:
    server_socket.sendto(bytes(word, 'utf-8'), (ip_address, port))

  for word in words:
    data, addr = server_socket.recvfrom(10000)
    print(str(data.decode("utf-8")))

  server_socket.sendto(bytes("#", 'utf-8'), (ip_address, port))

  #print("Type '#' to stop searching. Press <ENTER> to try another word.\n\n")
  #user_choice = str(input())
  #if(user_choice == "#"):
    #server_socket.sendto(bytes("#", 'utf-8'), (ip_address, port))
    #break
