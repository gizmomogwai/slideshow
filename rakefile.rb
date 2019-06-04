SSH_TARGET = "pi@192.168.0.140"

TARGET_PATH = "/home/pi/Pictures/ImageLib"

desc "Deploy slideshow to target"
task :deploy do
  sh "ssh #{SSH_TARGET} mkdir -p #{TARGET_PATH}"
  sh "scp build/libs/slideshow-all.jar #{SSH_TARGET}:#{TARGET_PATH}"
end

desc "Deploy images to target"
task :deploy_images do
  ["2013/06", "2012/06"].each do |path|
    year = path.split("/").first
    sh "ssh #{SSH_TARGET} mkdir -p #{TARGET_PATH}/#{year}"
    sh "scp -r ~/Pictures/ImageLib/#{path} #{SSH_TARGET}:#{TARGET_PATH}/#{year}"
  end
end

task :default => [:deploy]
