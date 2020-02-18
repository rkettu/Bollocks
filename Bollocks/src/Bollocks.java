import java.util.ArrayList;
import javafx.animation.* ;  // AnimationTimer, etc.
import javafx.util.Duration;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;  // Arc, Circle, etc.
import javafx.geometry.* ; // Point2D, etc.
import javafx.stage.Stage;
import javafx.scene.input.*;


class Ball extends Group
{
    private double velocity_x = 0;
    private double velocity_y = 0;
    private final double wall_bounciness = 0.65;      // 1 = perfect bounce, 0 = no bounce
    private int area_width = 0;
    private int area_height = 0;
    private final double slowing_factor = 0.003;          // How much ball slows down every 
    private final double MAX_VELOCITY = 100;

    Circle bouncer_bg;
    Rectangle  bouncing_area;
    double last_movement_x, last_movement_y;
    
    public Ball(Point2D given_position,
                Color given_color,
                Rectangle given_bouncing_area,
                int area_width,
                int area_height)
    {
        this.area_width = area_width;
        this.area_height = area_height;
        bouncer_bg = new Circle(given_position.getX(),
                                given_position.getY(),
                                32, given_color);

        bouncer_bg.setStroke(Color.BLACK);

        bouncing_area = given_bouncing_area;

        getChildren().add(bouncer_bg);
    }
    
    public void addVelX(double velocityAmount)
    {
        velocity_x += velocityAmount;
    }
    
    public void addVelY(double velocityAmount)
    {
        velocity_y += velocityAmount;
    }
    
    public void setVelX(double velocityAmount)
    {
        velocity_x = velocityAmount;
    }
    
    public void setVelY(double velocityAmount)
    {
        velocity_y = velocityAmount;
    }
    
    public double getVelX()
    {
        return velocity_x;
    }
    
    public double getVelY()
    {
        return velocity_y;
    }
    
    public void move()
    {
        if(velocity_x > MAX_VELOCITY)
        {
            velocity_x = MAX_VELOCITY;
        }
        if(velocity_y > MAX_VELOCITY)
        {
            velocity_y = MAX_VELOCITY;
        }
        velocity_x -= velocity_x * slowing_factor;
        velocity_y -= velocity_y * slowing_factor;
        
        
        
        bouncer_bg.setCenterX(bouncer_bg.getCenterX() + velocity_x);
        bouncer_bg.setCenterY(bouncer_bg.getCenterY() + velocity_y);
        
        // If ball hits NORTH wall
        if(bouncer_bg.getCenterY() - bouncer_bg.getRadius() <= bouncing_area.getY())
        {
            velocity_y = -velocity_y * wall_bounciness;
            bouncer_bg.setCenterY(bouncer_bg.getRadius());  // preventing ball going inside wall
        }
        
        // WEST
        if(bouncer_bg.getCenterX() - bouncer_bg.getRadius() <= bouncing_area.getX())
        {
            velocity_x = -velocity_x * wall_bounciness;
            bouncer_bg.setCenterX(bouncer_bg.getRadius());
        }

        // SOUTH
        if ( ( bouncer_bg.getCenterY()  +  bouncer_bg.getRadius() )
                 >= ( bouncing_area.getY()  +  bouncing_area.getHeight() ) )
        {
            velocity_y = -velocity_y * wall_bounciness;
            bouncer_bg.setCenterY(area_height - bouncer_bg.getRadius());
        }

        // EAST
        if ( ( bouncer_bg.getCenterX()  +  bouncer_bg.getRadius() )
                 >= ( bouncing_area.getX()  +  bouncing_area.getWidth() ) )
        {
            velocity_x = -velocity_x * wall_bounciness;
            bouncer_bg.setCenterX(area_width - bouncer_bg.getRadius());
        }
    }
}

public class Bollocks extends Application
{
    static final int SCENE_WIDTH = 800;
    static final int SCENE_HEIGHT = 680;
    static final int MAX_BALLS = 20;
    double velocity_boost = 1;

    AnimationTimer animation_timer ;
    
    private void handle_collisions(Ball a, Ball b)
    {
        if(a.getBoundsInParent().intersects(b.getBoundsInParent())) { 
                b.addVelX(a.getVelX());
                b.addVelY(a.getVelY());
        }
    }

    @Override
    public void start( Stage stage )
    {
        Group group_for_balls = new Group();

        stage.setTitle("Bollocks") ;

        Scene scene = new Scene(group_for_balls, SCENE_WIDTH, SCENE_HEIGHT);
        scene.setFill( Color.BLACK ) ;

        Rectangle bouncing_area =  new Rectangle(0, 0, SCENE_WIDTH, SCENE_HEIGHT);

        ArrayList<Ball> balls_list = new ArrayList<Ball>();
        
        Ball player_ball = new Ball(new Point2D(SCENE_WIDTH/2,SCENE_HEIGHT/2),
                                       Color.BLUE,
                                       bouncing_area,
                                       SCENE_WIDTH,
                                       SCENE_HEIGHT
                                       );

        balls_list.add(player_ball);
        
        group_for_balls.getChildren().add(player_ball);
        
        // Adds new ball to click location :-)
        scene.setOnMousePressed( ( MouseEvent event ) ->
        {
            if(balls_list.size() < MAX_BALLS)
            {
                Ball new_ball = new Ball(new Point2D(event.getSceneX(),event.getSceneY()),
                                           Color.RED,
                                           bouncing_area,
                                           SCENE_WIDTH,
                                           SCENE_HEIGHT
                                           );
                balls_list.add(new_ball);
                group_for_balls.getChildren().add(new_ball);
            }
        } ) ;
        
        scene.setOnKeyPressed((KeyEvent event) ->
        {
            if ( event.getCode()  ==  KeyCode.LEFT )
            {
                player_ball.addVelX(-velocity_boost);
            }
            if ( event.getCode()  ==  KeyCode.RIGHT )
            {
                player_ball.addVelX(velocity_boost);
            }
            if ( event.getCode()  ==  KeyCode.UP )
            {
                player_ball.addVelY(-velocity_boost);
            }
            if ( event.getCode()  ==  KeyCode.DOWN )
            {
                player_ball.addVelY(velocity_boost);
            }
        } ) ;
  
        stage.setScene( scene );
        stage.show();

        animation_timer = new AnimationTimer()
        {
            @Override
            public void handle(long timestamp_of_current_frame)
            {
                for (int i = 0; i < balls_list.size(); i++) {
                    balls_list.get(i).move();                  
                    for (int j = i+1; j < balls_list.size(); j++) { 
                        handle_collisions(balls_list.get(i), balls_list.get(j)); // Comparing each ball
                    }
                }
            }
        };
      
        animation_timer.start();
    }

    public static void main(String[] command_line_parameters)
    {
        launch(command_line_parameters);
    }
}