package org.petero.droidfish;

import java.util.ArrayList;
import java.util.List;

import org.petero.droidfish.gamelogic.Move;
import org.petero.droidfish.gamelogic.MoveGen;
import org.petero.droidfish.gamelogic.Piece;
import org.petero.droidfish.gamelogic.Position;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ChessBoard extends View {
	Position pos;

    int selectedSquare;
    float cursorX, cursorY;
    boolean cursorVisible;
    protected int x0, y0, sqSize;
    boolean flipped;
    boolean oneTouchMoves;
    
    List<Move> moveHints;

    protected Paint darkPaint;
    protected Paint brightPaint;
    private Paint redOutline;
    private Paint greenOutline;
    private Paint whitePiecePaint;
    private Paint blackPiecePaint;
    private ArrayList<Paint> moveMarkPaint;
    
	public ChessBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
    	pos = new Position();
        selectedSquare = -1;
        cursorX = cursorY = 0;
        cursorVisible = false;
        x0 = y0 = sqSize = 0;
        flipped = false;
        oneTouchMoves = false;

        darkPaint = new Paint();
        darkPaint.setARGB(255, 128, 128, 128);

        brightPaint = new Paint();
        brightPaint.setARGB(255, 190, 190, 90);

        redOutline = new Paint();
        redOutline.setARGB(255, 255, 0, 0);
        redOutline.setStyle(Paint.Style.STROKE);
        
        greenOutline = new Paint();
        greenOutline.setARGB(255, 0, 255, 0);
        greenOutline.setStyle(Paint.Style.STROKE);
        
        whitePiecePaint = new Paint();
        whitePiecePaint.setARGB(255, 255, 255, 255);
        whitePiecePaint.setAntiAlias(true);
        
        blackPiecePaint = new Paint();
        blackPiecePaint.setARGB(255, 0, 0, 0);
        blackPiecePaint.setAntiAlias(true);
        
        moveMarkPaint = new ArrayList<Paint>();
        for (int i = 0; i < 6; i++) {
        	Paint p = new Paint();
        	switch (i) {
        	case 0: p.setARGB(160,  31, 31, 255);  break;
        	case 1: p.setARGB(160, 255, 31,  31);  break;
        	case 2: p.setARGB( 80,  31, 31, 255);  break;
        	case 3: p.setARGB( 80, 255, 31,  31);  break;
        	case 4: p.setARGB( 30,  31, 31, 255);  break;
        	case 5: p.setARGB( 40, 255, 31,  31);  break;
        	}
        	p.setStyle(Paint.Style.FILL);
        	p.setAntiAlias(true);
        	moveMarkPaint.add(p);
        }

        Typeface chessFont = Typeface.createFromAsset(getContext().getAssets(), "ChessCases.ttf");
		whitePiecePaint.setTypeface(chessFont);
		blackPiecePaint.setTypeface(chessFont);
	}

	/**
     * Set the board to a given state.
     * @param pos
     */
    final public void setPosition(Position pos) {
    	if (!this.pos.equals(pos)) {
    		this.pos = new Position(pos);
    		invalidate();
    	}
    }

    /**
     * Set/clear the board flipped status.
     * @param flipped
     */
    final public void setFlipped(boolean flipped) {
    	if (this.flipped != flipped) {
    		this.flipped = flipped;
    		invalidate();
    	}
    }

    /**
     * Set/clear the selected square.
     * @param square The square to select, or -1 to clear selection.
     */
    final public void setSelection(int square) {
        if (square != selectedSquare) {
            selectedSquare = square;
            invalidate();
        }
    }

	protected int getWidth(int sqSize) { return sqSize * 8 + 4; }
	protected int getHeight(int sqSize) { return sqSize * 8 + 4; }
	protected int getSqSizeW(int width) { return (width - 4) / 8; }
	protected int getSqSizeH(int height) { return (height - 4) / 8; }

	protected int getMaxHeightPercentage() { return 75; }
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		int sqSizeW = getSqSizeW(width);
		int sqSizeH = getSqSizeH(height);
		int sqSize = Math.min(sqSizeW, sqSizeH);
		if (height > width) {
			int p = getMaxHeightPercentage();
			height = Math.min(getHeight(sqSize), height * p / 100);
		} else {
			width = Math.min(getWidth(sqSize), width * 65 / 100);
		}
		setMeasuredDimension(width, height);
	}

	protected void computeOrigin(int width, int height) {
        x0 = (width - sqSize * 8) / 2;
        y0 = (height - sqSize * 8) / 2;
	}

	protected int getXFromSq(int sq) { return Position.getX(sq); }
	protected int getYFromSq(int sq) { return Position.getY(sq); }

	@Override
	protected void onDraw(Canvas canvas) {
		final int width = getWidth();
		final int height = getHeight();
        sqSize = Math.min(getSqSizeW(width), getSqSizeH(height));
		computeOrigin(width, height);
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                final int xCrd = getXCrd(x);
                final int yCrd = getYCrd(y);
                Paint paint = Position.darkSquare(x, y) ? darkPaint : brightPaint;
                canvas.drawRect(xCrd, yCrd, xCrd+sqSize, yCrd+sqSize, paint);

                int sq = Position.getSquare(x, y);
                int p = pos.getPiece(sq);
                drawPiece(canvas, xCrd + sqSize / 2, yCrd + sqSize / 2, p);
            }
        }
        drawExtraSquares(canvas);
        if (selectedSquare != -1) {
            int selX = getXFromSq(selectedSquare);
            int selY = getYFromSq(selectedSquare);
            redOutline.setStrokeWidth(sqSize/(float)16);
            int x0 = getXCrd(selX);
            int y0 = getYCrd(selY);
            canvas.drawRect(x0, y0, x0 + sqSize, y0 + sqSize, redOutline);
        }
        if (cursorVisible) {
        	int x = Math.round(cursorX);
        	int y = Math.round(cursorY);
        	int x0 = getXCrd(x);
        	int y0 = getYCrd(y);
            greenOutline.setStrokeWidth(sqSize/(float)16);
            canvas.drawRect(x0, y0, x0 + sqSize, y0 + sqSize, greenOutline);
        }
        drawMoveHints(canvas);
    }

	private final void drawMoveHints(Canvas canvas) {
		if (moveHints == null)
			return;
		float h = (float)(sqSize / 2.0);
		float d = (float)(sqSize / 8.0);
		double v = 35 * Math.PI / 180;
		double cosv = Math.cos(v);
		double sinv = Math.sin(v);
		double tanv = Math.tan(v);
        int n = Math.min(moveMarkPaint.size(), moveHints.size());
		for (int i = 0; i < n; i++) {
			Move m = moveHints.get(i);
			float x0 = getXCrd(Position.getX(m.from)) + h;
			float y0 = getYCrd(Position.getY(m.from)) + h;
			float x1 = getXCrd(Position.getX(m.to)) + h;
			float y1 = getYCrd(Position.getY(m.to)) + h;

			float x2 = (float)(Math.hypot(x1 - x0, y1 - y0) + d);
			float y2 = 0;
			float x3 = (float)(x2 - h * cosv);
			float y3 = (float)(y2 - h * sinv);
			float x4 = (float)(x3 - d * sinv);
			float y4 = (float)(y3 + d * cosv);
			float x5 = (float)(x4 + (-d/2 - y4) / tanv);
			float y5 = (float)(-d / 2);
			float x6 = 0;
			float y6 = y5 / 2;
			Path path = new Path();
			path.moveTo(x2, y2);
			path.lineTo(x3, y3);
//			path.lineTo(x4, y4);
			path.lineTo(x5, y5);
			path.lineTo(x6, y6);
			path.lineTo(x6, -y6);
			path.lineTo(x5, -y5);
//			path.lineTo(x4, -y4);
			path.lineTo(x3, -y3);
			path.close();
			Matrix mtx = new Matrix();
			mtx.postRotate((float)(Math.atan2(y1 - y0, x1 - x0) * 180 / Math.PI));
			mtx.postTranslate(x0, y0);
			path.transform(mtx);
			Paint p = moveMarkPaint.get(i);
			canvas.drawPath(path, p);
		}
	}
	
    protected void drawExtraSquares(Canvas canvas) {
	}

	protected final void drawPiece(Canvas canvas, int xCrd, int yCrd, int p) {
        char c = 0;
        switch (p) {
        	default:
            case Piece.EMPTY:   c = 0;   break;
            case Piece.WKING:   c = 'H'; break;
            case Piece.WQUEEN:  c = 'I'; break;
            case Piece.WROOK:   c = 'J'; break;
            case Piece.WBISHOP: c = 'K'; break;
            case Piece.WKNIGHT: c = 'L'; break;
            case Piece.WPAWN:   c = 'M'; break;
            case Piece.BKING:   c = 'N'; break;
            case Piece.BQUEEN:  c = 'O'; break;
            case Piece.BROOK:   c = 'P'; break;
            case Piece.BBISHOP: c = 'Q'; break;
            case Piece.BKNIGHT: c = 'R'; break;
            case Piece.BPAWN:   c = 'S'; break;
        }
        if (c != 0) {
            String psb = Character.toString(c);
            String psw = Character.toString((char)(c + 'k' - 'H'));
        	blackPiecePaint.setTextSize(sqSize);
        	whitePiecePaint.setTextSize(sqSize);
            Rect bounds = new Rect();
            blackPiecePaint.getTextBounds("H", 0, 1, bounds);
            int xCent = bounds.centerX();
            int yCent = bounds.centerY();
            canvas.drawText(psw, xCrd - xCent, yCrd - yCent, whitePiecePaint);
            canvas.drawText(psb, xCrd - xCent, yCrd - yCent, blackPiecePaint);
        }
    }

    protected int getXCrd(int x) { return x0 + sqSize * (flipped ? 7 - x : x); }
    protected int getYCrd(int y) { return y0 + sqSize * (flipped ? y : 7 - y); }
    protected int getXSq(int xCrd) { int t = (xCrd - x0) / sqSize; return flipped ? 7 - t : t; }
    protected int getYSq(int yCrd) { int t = (yCrd - y0) / sqSize; return flipped ? t : 7 - t; }

    /**
     * Compute the square corresponding to the coordinates of a mouse event.
     * @param evt Details about the mouse event.
     * @return The square corresponding to the mouse event, or -1 if outside board.
     */
    int eventToSquare(MotionEvent evt) {
        int xCrd = (int)(evt.getX());
        int yCrd = (int)(evt.getY());

        int sq = -1;
        if (sqSize > 0) {
            int x = getXSq(xCrd);
            int y = getYSq(yCrd);
            if ((x >= 0) && (x < 8) && (y >= 0) && (y < 8)) {
                sq = Position.getSquare(x, y);
            }
        }
        return sq;
    }

    final private boolean myColor(int piece) {
    	return (piece != Piece.EMPTY) && (Piece.isWhite(piece) == pos.whiteMove);
    }

	Move mousePressed(int sq) {
		if (sq < 0)
			return null;
    	cursorVisible = false;
        if (selectedSquare != -1) {
        	int p = pos.getPiece(selectedSquare);
        	if (!myColor(p)) {
        		setSelection(-1); // Remove selection of opponents last moving piece
        	}
        }

        int p = pos.getPiece(sq);
        if (selectedSquare != -1) {
            if (sq != selectedSquare) {
            	if (!myColor(p)) {
                    Move m = new Move(selectedSquare, sq, Piece.EMPTY);
                    setSelection(sq);
                    return m;
                }
            }
            setSelection(-1);
        } else {
        	if (oneTouchMoves) {
                ArrayList<Move> moves = new MoveGen().pseudoLegalMoves(pos);
                moves = MoveGen.removeIllegal(pos, moves);
                Move matchingMove = null;
                int toSq = -1;
                for (Move m : moves) {
                	if ((m.from == sq) || (m.to == sq)) {
                		if (matchingMove == null) {
                			matchingMove = m;
                			toSq = m.to;
                		} else {
                			matchingMove = null;
                			break;
                		}
                	}
                }
                if (matchingMove != null) {
                	setSelection(toSq);
                	return matchingMove;
                }
        	}
        	if (myColor(p)) {
        		setSelection(sq);
        	}
        }
        return null;
    }

	public static class OnTrackballListener {
    	public void onTrackballEvent(MotionEvent event) { }
	}
	private OnTrackballListener otbl = null;
	public final void setOnTrackballListener(OnTrackballListener onTrackballListener) {
		otbl = onTrackballListener;
	}
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (otbl != null) {
			otbl.onTrackballEvent(event);
			return true;
		}
		return false;
	}

	protected int minValidY() { return 0; }
	protected int getSquare(int x, int y) { return Position.getSquare(x, y); }
	
	public final Move handleTrackballEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			invalidate();
			if (cursorVisible) {
				int x = Math.round(cursorX);
				int y = Math.round(cursorY);
				cursorX = x;
				cursorY = y;
				int sq = getSquare(x, y);
				return mousePressed(sq);
			}
			return null;
		}
		cursorVisible = true;
		int c = flipped ? -1 : 1;
		cursorX += c * event.getX();
		cursorY -= c * event.getY();
		if (cursorX < 0) cursorX = 0;
		if (cursorX > 7) cursorX = 7;
		if (cursorY < minValidY()) cursorY = minValidY();
		if (cursorY > 7) cursorY = 7;
		invalidate();
		return null;
	}

	public final void setMoveHints(List<Move> moveHints) {
		boolean equal = false;
		if ((this.moveHints == null) || (moveHints == null)) {
			equal = this.moveHints == moveHints;
		} else {
			equal = this.moveHints.equals(moveHints);
		}
		if (!equal) {
			this.moveHints = moveHints;
			invalidate();
		}
	}
}
