import socket
 

ip_address = socket.gethostbyname("localhost")
port = 9876
port_for_meaning = 9880
server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
i = 0
while(i<5):
  #word = input("Enter a word you want to search meaning for\n")

  #while(not word):
    #word = input("You Entered a empty word. Enter a valid word\n")


  #print(ip_address, port)
  #print("searching meaning for", word)
  words = ["abet", "hi", "asdasdas", "apple", "orange"]*10;
  for word in words:
    server_socket.sendto(bytes(word, 'utf-8'), (ip_address, port))

  for word in words:
    data, addr = server_socket.recvfrom(10000)
    response = str(data.decode("utf-8"))
    if("was not found in the dictionary" in response):
      print(response)
      meaning = input("please enter the meaning you want to add"
                      "\n")
      server_socket.sendto(bytes(meaning, 'utf-8'), (ip_address, port_for_meaning))
    else:
      print(response)


  i= i+1


server_socket.sendto(bytes("#", 'utf-8'), (ip_address, port))

  #print("Type '#' to stop searching. Press <ENTER> to try another word.\n\n")
  #user_choice = str(input())
  #if(user_choice == "#"):
    #server_socket.sendto(bytes("#", 'utf-8'), (ip_address, port))
    #break
